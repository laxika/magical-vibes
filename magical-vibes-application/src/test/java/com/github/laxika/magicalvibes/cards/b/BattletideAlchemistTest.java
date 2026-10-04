package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ChangelingSentinel;
import com.github.laxika.magicalvibes.cards.l.Lignify;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WarSpikeChangeling;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BattletideAlchemist.class, ChangelingSentinel.class, Lignify.class, Shock.class, WarSpikeChangeling.class})
class BattletideAlchemistTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents 1 damage from a source (X=1, only the Alchemist is a Cleric)")
    void preventsOneNoncombatDamage() {
        // Battletide Alchemist is itself a Cleric, so X = 1.
        harness.addToBattlefield(player1, new BattletideAlchemist());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        acceptAllPreventionChoices();

        // Shock deals 2; 1 is prevented, so player1 takes 1.
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("X scales with the number of Clerics controlled")
    void preventionScalesWithClerics() {
        // Alchemist + Changeling Sentinel = 2 Clerics, so X = 2.
        harness.addToBattlefield(player1, new BattletideAlchemist());
        harness.addToBattlefield(player1, new ChangelingSentinel());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        acceptAllPreventionChoices();

        // Shock deals 2; all of it is prevented.
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The Alchemist's controller may protect an opponent")
    void mayPreventDamageToOpponent() {
        harness.addToBattlefield(player1, new BattletideAlchemist());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Applies the prevention amount separately to each noncombat source")
    void preventsDamageFromEachNoncombatSource() {
        harness.addToBattlefield(player1, new BattletideAlchemist());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        acceptAllPreventionChoices();
        harness.castAndResolveInstant(player2, 0, player1.getId());
        acceptAllPreventionChoices();

        // Each Shock is a separate source, so each deals 1 after prevention.
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Prevents X of each attacker's combat damage to the controller")
    void preventsCombatDamagePerAttacker() {
        harness.addToBattlefield(player1, new BattletideAlchemist());
        harness.setLife(player1, 20);

        // player2 attacks player1 with a 3/3.
        addCreatureReady(player2, new WarSpikeChangeling());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);
        acceptAllPreventionChoices();

        // War-Spike Changeling deals 3; 1 (X=1) is prevented, so player1 takes 2.
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Prevents X from each combat source independently")
    void preventsCombatDamageFromEachAttacker() {
        harness.addToBattlefield(player1, new BattletideAlchemist());
        harness.setLife(player1, 20);
        addCreatureReady(player2, new WarSpikeChangeling());
        addCreatureReady(player2, new WarSpikeChangeling());

        declareAttackersAndPrepareBlockers(player2, List.of(0, 1));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);
        acceptAllPreventionChoices();

        // Each 3/3 is a separate source, so each has 1 damage prevented.
        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("The Alchemist's controller may decline prevention of damage to themselves")
    void mayDeclinePrevention() {
        harness.addToBattlefield(player1, new BattletideAlchemist());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, false);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Does not prevent damage to a creature")
    void doesNotPreventCreatureDamage() {
        harness.addToBattlefield(player1, new BattletideAlchemist());
        var sentinel = harness.addToBattlefieldAndReturn(player1, new ChangelingSentinel());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, sentinel.getId());

        harness.assertNotOnBattlefield(player1, "Changeling Sentinel");
        harness.assertInGraveyard(player1, "Changeling Sentinel");
    }

    @Test
    @DisplayName("Counts Clerics at the time damage would be dealt")
    void recalculatesClericCountAfterClericDies() {
        harness.addToBattlefield(player1, new BattletideAlchemist());
        var sentinel = harness.addToBattlefieldAndReturn(player1, new ChangelingSentinel());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, sentinel.getId());
        harness.assertInGraveyard(player1, "Changeling Sentinel");
        harness.castAndResolveInstant(player2, 0, player1.getId());
        acceptAllPreventionChoices();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Each Alchemist can prevent damage using the full Cleric count")
    void multipleAlchemistsApplyIndependently() {
        harness.addToBattlefield(player1, new BattletideAlchemist());
        harness.addToBattlefield(player1, new BattletideAlchemist());
        harness.setLife(player1, 20);
        addCreatureReady(player2, new WarSpikeChangeling());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);
        acceptAllPreventionChoices();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An Alchemist that loses all abilities cannot prevent damage")
    void losingAbilitiesDisablesPrevention() {
        var alchemist = harness.addToBattlefieldAndReturn(player1, new BattletideAlchemist());
        harness.addToBattlefield(player1, new ChangelingSentinel());
        harness.setHand(player2, List.of(new Lignify(), new Shock()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.castEnchantment(player2, 0, alchemist.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Lignify");
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
    }
    private void acceptAllPreventionChoices() {
        while (gd.interaction.activeInteraction() instanceof
                com.github.laxika.magicalvibes.model.PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }
    }

}
