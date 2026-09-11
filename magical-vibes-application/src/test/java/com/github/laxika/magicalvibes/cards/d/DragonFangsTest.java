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

@CardUsed({DragonFangs.class, GrizzlyBears.class, KrosanCloudscraper.class})
class DragonFangsTest extends BaseCardTest {

    @Test
    void resolvingAuraBoostsEnchantedCreatureAndGrantsTrample() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DragonFangs()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Dragon Fangs");
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void highManaValueCreatureEnteringUnderAnyPlayersControlTriggersReturn() {
        DragonFangs fangs = new DragonFangs();
        harness.setGraveyard(player1, List.of(fangs));
        Permanent creature = harness.enterBattlefieldAndReturn(player2, new KrosanCloudscraper());

        resolveMayAbility(true);

        Permanent returnedFangs = findPermanent(player1, "Dragon Fangs");
        assertThat(returnedFangs.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(14);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(14);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        harness.assertNotInGraveyard(player1, "Dragon Fangs");
    }

    @Test
    void smallerCreatureDoesNotTriggerReturn() {
        harness.setGraveyard(player1, List.of(new DragonFangs()));
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Dragon Fangs");
    }

    @Test
    void decliningReturnKeepsAuraInGraveyard() {
        harness.setGraveyard(player1, List.of(new DragonFangs()));
        harness.enterBattlefieldAndReturn(player1, new KrosanCloudscraper());

        resolveMayAbility(false);

        harness.assertInGraveyard(player1, "Dragon Fangs");
        harness.assertNotOnBattlefield(player1, "Dragon Fangs");
    }

    private void resolveMayAbility(boolean accepted) {
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, accepted);
        resolveAllTriggers();
    }
}
