package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ChartoothCougar;
import com.github.laxika.magicalvibes.cards.g.GoblinBrigand;
import com.github.laxika.magicalvibes.cards.s.SoulCollector;
import com.github.laxika.magicalvibes.cards.w.WirewoodGuardian;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonWings.class, GoblinBrigand.class, WirewoodGuardian.class, ChartoothCougar.class,
        SoulCollector.class})
class DragonWingsTest extends BaseCardTest {

    @Test
    void resolvingAuraGrantsFlyingToEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new GoblinBrigand());
        harness.setHand(player1, List.of(new DragonWings()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Dragon Wings");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    void cyclingDrawsACardAndPutsAuraInGraveyard() {
        harness.setHand(player1, List.of(new DragonWings()));
        harness.setLibrary(player1, List.of(new GoblinBrigand()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dragon Wings");
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Goblin Brigand"));
    }

    @Test
    void highManaValueCreatureEnteringTriggersReturnAttachedToIt() {
        harness.setGraveyard(player1, List.of(new DragonWings()));
        Permanent creature = harness.enterBattlefieldAndReturn(player2, new WirewoodGuardian());

        resolveMayAbility(true);

        Permanent returnedWings = findPermanent(player1, "Dragon Wings");
        assertThat(returnedWings.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        harness.assertNotInGraveyard(player1, "Dragon Wings");
    }

    @Test
    void manaValueSixCreatureEnteringTriggersReturnAttachedToIt() {
        harness.setGraveyard(player1, List.of(new DragonWings()));
        Permanent creature = harness.enterBattlefieldAndReturn(player2, new ChartoothCougar());

        resolveMayAbility(true);

        Permanent returnedWings = findPermanent(player1, "Dragon Wings");
        assertThat(returnedWings.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        harness.assertNotInGraveyard(player1, "Dragon Wings");
    }

    @Test
    void smallerCreatureDoesNotTriggerReturn() {
        harness.setGraveyard(player1, List.of(new DragonWings()));
        harness.enterBattlefieldAndReturn(player1, new SoulCollector());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Dragon Wings");
    }

    @Test
    void decliningReturnKeepsAuraInGraveyard() {
        harness.setGraveyard(player1, List.of(new DragonWings()));
        harness.enterBattlefieldAndReturn(player1, new WirewoodGuardian());

        resolveMayAbility(false);

        harness.assertInGraveyard(player1, "Dragon Wings");
        harness.assertNotOnBattlefield(player1, "Dragon Wings");
    }

    @Test
    void acceptedReturnEntersUnattachedWhenEnteringCreatureLeavesBeforeResolution() {
        harness.setGraveyard(player1, List.of(new DragonWings()));
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new WirewoodGuardian());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gameLogContains("Dragon Wings returns to the battlefield unattached.")).isTrue();
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
