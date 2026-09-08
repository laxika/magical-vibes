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

@CardUsed({DragonWings.class, GrizzlyBears.class, KrosanCloudscraper.class})
class DragonWingsTest extends BaseCardTest {

    @Test
    void resolvingAuraGrantsFlyingToEnchantedCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DragonWings()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Dragon Wings");
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    void cyclingDrawsACardAndPutsAuraInGraveyard() {
        harness.setHand(player1, List.of(new DragonWings()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dragon Wings");
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    void highManaValueCreatureEnteringTriggersReturnAttachedToIt() {
        harness.setGraveyard(player1, List.of(new DragonWings()));
        Permanent creature = harness.enterBattlefieldAndReturn(player2, new KrosanCloudscraper());

        resolveMayAbility(true);

        Permanent returnedWings = findPermanent(player1, "Dragon Wings");
        assertThat(returnedWings.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        harness.assertNotInGraveyard(player1, "Dragon Wings");
    }

    @Test
    void smallerCreatureDoesNotTriggerReturn() {
        harness.setGraveyard(player1, List.of(new DragonWings()));
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Dragon Wings");
    }

    @Test
    void decliningReturnKeepsAuraInGraveyard() {
        harness.setGraveyard(player1, List.of(new DragonWings()));
        harness.enterBattlefieldAndReturn(player1, new KrosanCloudscraper());

        resolveMayAbility(false);

        harness.assertInGraveyard(player1, "Dragon Wings");
        harness.assertNotOnBattlefield(player1, "Dragon Wings");
    }

    private void resolveMayAbility(boolean accepted) {
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, accepted);
        resolveAllTriggers();
    }
}
