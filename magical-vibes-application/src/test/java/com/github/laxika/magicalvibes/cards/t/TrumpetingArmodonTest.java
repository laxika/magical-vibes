package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DauthiMarauder;
import com.github.laxika.magicalvibes.cards.l.LotusPetal;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrumpetingArmodon.class, TrainedArmodon.class, DauthiMarauder.class, LotusPetal.class})
class TrumpetingArmodonTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability makes the target block the Armodon")
    void resolvingAbilityAddsMustBlockRestriction() {
        Permanent armodon = addCreatureReady(player1, new TrumpetingArmodon());
        Permanent target = addCreatureReady(player2, new TrainedArmodon());
        giveMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMustBlockIds()).contains(armodon.getId());
    }

    @Test
    @DisplayName("Cannot activate the ability without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new TrumpetingArmodon());
        Permanent target = addCreatureReady(player2, new TrainedArmodon());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The ability does not tap the Armodon")
    void abilityDoesNotTapSource() {
        Permanent armodon = addCreatureReady(player1, new TrumpetingArmodon());
        Permanent target = addCreatureReady(player2, new TrainedArmodon());
        giveMana();

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(armodon.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Targeted creature must block the attacking Armodon")
    void targetedCreatureMustBlock() {
        Permanent armodon = addCreatureReady(player1, new TrumpetingArmodon());
        Permanent blocker = addCreatureReady(player2, new TrainedArmodon());
        giveMana();

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        armodon.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("A targeted creature that cannot legally block is not required to block")
    void targetedDauthiMarauderDoesNotHaveToBlockWithoutShadow() {
        Permanent armodon = addCreatureReady(player1, new TrumpetingArmodon());
        Permanent marauder = addCreatureReady(player2, new DauthiMarauder());
        giveMana();

        harness.activateAbility(player1, 0, null, marauder.getId());
        harness.passBothPriorities();

        armodon.setAttacking(true);
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
        assertThat(marauder.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("The ability can target only a creature")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new TrumpetingArmodon());
        Permanent lotusPetal = harness.addToBattlefieldAndReturn(player2, new LotusPetal());
        giveMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, lotusPetal.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability fizzles if its target leaves the battlefield before resolution")
    void abilityFizzlesIfTargetLeavesBattlefield() {
        Permanent armodon = addCreatureReady(player1, new TrumpetingArmodon());
        Permanent target = addCreatureReady(player2, new TrainedArmodon());
        giveMana();

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.getMustBlockIds()).doesNotContain(armodon.getId());
    }

    @Test
    @DisplayName("Requirement lapses at end of turn")
    void restrictionResetsAtEndOfTurn() {
        Permanent armodon = addCreatureReady(player1, new TrumpetingArmodon());
        Permanent blocker = addCreatureReady(player2, new TrainedArmodon());
        giveMana();

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();
        assertThat(blocker.getMustBlockIds()).contains(armodon.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getMustBlockIds()).isEmpty();
    }

    private void giveMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

}
