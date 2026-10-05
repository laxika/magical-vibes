package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DireFleetCaptain;
import com.github.laxika.magicalvibes.cards.r.RavenousDaggertooth;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({LightningRigCrew.class, DireFleetCaptain.class, RavenousDaggertooth.class})
class LightningRigCrewTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability deals 1 damage to each opponent")
    void tapAbilityDealsDamage() {
        addCrewReady(player1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Tap ability taps the creature")
    void tapAbilityTapsCreature() {
        Permanent perm = addCrewReady(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(perm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting a Pirate spell triggers untap")
    void pirateSpellTriggersUntap() {
        addCrewReady(player1);
        harness.setHand(player1, List.of(new DireFleetCaptain()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        // Dire Fleet Captain on stack + triggered ability
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Lightning-Rig Crew"));
    }

    @Test
    @DisplayName("Resolving Pirate spell trigger untaps the crew")
    void pirateTriggerUntapsCrew() {
        Permanent perm = addCrewReady(player1);
        perm.tap();
        assertThat(perm.isTapped()).isTrue();

        harness.setHand(player1, List.of(new DireFleetCaptain()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        // Resolve the triggered ability (LIFO — trigger on top)
        harness.passBothPriorities();

        assertThat(perm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Casting a non-Pirate creature does not trigger untap")
    void nonPirateDoesNotTrigger() {
        addCrewReady(player1);
        harness.setHand(player1, List.of(new RavenousDaggertooth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        // Only the creature spell on the stack, no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Opponent casting a Pirate spell does not trigger controller's crew")
    void opponentPirateDoesNotTrigger() {
        addCrewReady(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new DireFleetCaptain()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castCreature(player2, 0);

        GameData gd = harness.getGameData();
        // Only the creature spell on stack, no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Can tap for damage, then cast Pirate to untap and tap again")
    void tapUntapTapAgain() {
        Permanent perm = addCrewReady(player1);
        harness.setLife(player2, 20);

        // First activation: tap to deal 1 damage
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        assertThat(perm.isTapped()).isTrue();

        // Cast a Pirate spell to untap
        harness.setHand(player1, List.of(new DireFleetCaptain()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        // Resolve the triggered untap ability
        harness.passBothPriorities();
        assertThat(perm.isTapped()).isFalse();

        // Resolve the Pirate creature spell
        harness.passBothPriorities();

        // Second activation: tap again to deal 1 more damage
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A summoning-sick crew cannot activate its tap ability")
    void summoningSickCrewCannotActivate() {
        harness.addToBattlefield(player1, new LightningRigCrew());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Each crew untaps itself when its controller casts a Pirate")
    void pirateSpellUntapsEachCrewSeparately() {
        Permanent first = addCrewReady(player1);
        Permanent second = addCrewReady(player1);
        Permanent opposing = addCrewReady(player2);
        first.tap();
        second.tap();
        opposing.tap();
        harness.setHand(player1, List.of(new DireFleetCaptain()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        assertThat(List.of(first, second).stream().filter(p -> !p.isTapped()).count()).isEqualTo(1);
        assertThat(opposing.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(opposing.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A Pirate entering without being cast does not untap the crew")
    void pirateEnteringWithoutCastDoesNotUntap() {
        Permanent crew = addCrewReady(player1);
        crew.tap();

        harness.enterBattlefieldAndReturn(player1, new DireFleetCaptain());

        assertThat(gd.stack).isEmpty();
        assertThat(crew.isTapped()).isTrue();
    }

    private Permanent addCrewReady(Player player) {
        return addCreatureReady(player, new LightningRigCrew());
    }
}
