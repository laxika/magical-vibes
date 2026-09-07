package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilumgarAssassin.class, GrizzlyBears.class, HillGiant.class})
class SilumgarAssassinTest extends BaseCardTest {

    @Test
    void megamorphPutsCounterOnItAndDestroysTargetOpponentCreatureWithPowerAtMostThree() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent assassin = castFaceDown();

        turnFaceUp(assassin);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(assassin.isFaceDown()).isFalse();
        assertThat(assassin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
    }

    @Test
    void megamorphHasNoTargetWhenOnlyOwnOrHigherPowerOpponentCreaturesExist() {
        harness.addToBattlefield(player1, new HillGiant());
        Permanent highPowerCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        highPowerCreature.setPowerModifier(1);
        Permanent assassin = castFaceDown();

        turnFaceUp(assassin);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(assassin.isFaceDown()).isFalse();
        assertThat(assassin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    void creaturesWithGreaterPowerCannotBlockIt() {
        Permanent assassin = addCreatureReady(player1, new SilumgarAssassin());
        assassin.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new HillGiant());

        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(assassin);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power less than or equal to this creature's power");
    }

    @Test
    void creatureWithEqualPowerCanBlockIt() {
        Permanent assassin = addCreatureReady(player1, new SilumgarAssassin());
        assassin.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(assassin);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new SilumgarAssassin()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return findPermanent(player1, "Silumgar Assassin");
    }

    private void turnFaceUp(Permanent assassin) {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(assassin));
    }
}
