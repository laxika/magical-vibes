package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KrosanCloudscraper;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonScales.class, GrizzlyBears.class, KrosanCloudscraper.class})
class DragonScalesTest extends BaseCardTest {

    @Test
    void resolvingAuraBoostsEnchantedCreatureAndGrantsVigilance() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DragonScales()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Dragon Scales");
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void highManaValueCreatureEnteringUnderAnyPlayersControlTriggersReturn() {
        DragonScales scales = new DragonScales();
        harness.setGraveyard(player1, List.of(scales));
        Permanent creature = harness.enterBattlefieldAndReturn(player2, new KrosanCloudscraper());

        resolveMayAbility(true);

        Permanent returnedScales = findPermanent(player1, "Dragon Scales");
        assertThat(returnedScales.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(14);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(15);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        harness.assertNotInGraveyard(player1, "Dragon Scales");
    }

    @Test
    void smallerCreatureDoesNotTriggerReturn() {
        harness.setGraveyard(player1, List.of(new DragonScales()));
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Dragon Scales");
    }

    @Test
    void decliningReturnKeepsAuraInGraveyard() {
        harness.setGraveyard(player1, List.of(new DragonScales()));
        harness.enterBattlefieldAndReturn(player1, new KrosanCloudscraper());

        resolveMayAbility(false);

        harness.assertInGraveyard(player1, "Dragon Scales");
        harness.assertNotOnBattlefield(player1, "Dragon Scales");
    }

    private void resolveMayAbility(boolean accepted) {
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, accepted);
        resolveAllTriggers();
    }
}
