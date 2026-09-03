package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AbyssalHunter;
import com.github.laxika.magicalvibes.cards.g.GiantMantis;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CircleOfDespair.class, AbyssalHunter.class, GiantMantis.class, Incinerate.class})
class CircleOfDespairTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability sacrifices the chosen creature and puts the ability on the stack")
    void activatingSacrificesCreature() {
        addReadyCircle(player1);
        Permanent mantis = addCreatureReady(player1, new GiantMantis());
        addCreatureReady(player1, new AbyssalHunter());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, mantis.getId());

        harness.assertNotOnBattlefield(player1, "Giant Mantis");
        harness.assertInGraveyard(player1, "Giant Mantis");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Choosing a source records a one-shot any-target prevention shield")
    void choosingSourceRecordsShield() {
        addReadyCircle(player1);
        addCreatureReady(player1, new GiantMantis());
        Permanent source = addCreatureReady(player2, new GiantMantis());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, source.getId());

        assertThat(gd.sourceNextDamageToAnyTargetShields)
                .extracting(s -> s.sourceId())
                .containsExactly(source.getId());
    }

    @Test
    @DisplayName("Prevents the next noncombat damage from the chosen source to another player's creature")
    void preventsNoncombatDamageToCreature() {
        addReadyCircle(player1);
        Permanent fodder = addCreatureReady(player1, new GiantMantis());
        Permanent hunter = addCreatureReady(player1, new AbyssalHunter());
        Permanent victim = addCreatureReady(player2, new GiantMantis());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, hunter.getId());

        harness.activateAbility(player1, indexOf(player1, hunter), null, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isEqualTo(0);
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("Prevents damage from a chosen spell to a creature")
    void preventsDamageFromChosenSpellToCreature() {
        addReadyCircle(player1);
        Permanent fodder = addCreatureReady(player1, new GiantMantis());
        addCreatureReady(player1, new AbyssalHunter());
        Permanent victim = addCreatureReady(player2, new GiantMantis());
        Incinerate incinerate = new Incinerate();
        harness.setHand(player2, List.of(incinerate));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player2, 0, victim.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, incinerate.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Giant Mantis");
        assertThat(victim.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Prevents combat damage from the chosen attacker to the controller")
    void preventsCombatDamageToPlayer() {
        harness.setLife(player1, 20);
        addReadyCircle(player1);
        addCreatureReady(player1, new GiantMantis());
        Permanent attacker = addCreatureReady(player2, new GiantMantis());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, attacker.getId());

        harness.forceActivePlayer(player2);
        attacker.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("Damage from a source other than the chosen one is not prevented")
    void doesNotAffectNonChosenSource() {
        addReadyCircle(player1);
        Permanent fodder = addCreatureReady(player1, new GiantMantis());
        Permanent hunter = addCreatureReady(player1, new AbyssalHunter());
        Permanent decoy = addCreatureReady(player1, new GiantMantis());
        Permanent victim = addCreatureReady(player2, new GiantMantis());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, decoy.getId());

        harness.activateAbility(player1, indexOf(player1, hunter), null, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.sourceNextDamageToAnyTargetShields)
                .extracting(s -> s.sourceId())
                .containsExactly(decoy.getId());
    }

    @Test
    @DisplayName("Shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        addReadyCircle(player1);
        addCreatureReady(player1, new GiantMantis());
        Permanent source = addCreatureReady(player2, new GiantMantis());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        assertThat(gd.sourceNextDamageToAnyTargetShields).isNotEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("Ability cannot be activated without a creature to sacrifice")
    void cannotActivateWithoutCreature() {
        addReadyCircle(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    private Permanent addReadyCircle(Player player) {
        return addCreatureReady(player, new CircleOfDespair());
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
