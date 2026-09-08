package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KaronasZealot.class, HillGiant.class, ProdigalPyromancer.class, GrizzlyBears.class, Plains.class})
class KaronasZealotTest extends BaseCardTest {

    @Test
    void turningFaceUpRedirectsAllDamageToTheChosenCreature() {
        Permanent zealot = castFaceDown();
        Permanent target = addCreatureReady(player2, new HillGiant());
        Permanent firstPyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        Permanent secondPyromancer = addCreatureReady(player1, new ProdigalPyromancer());

        turnFaceUp(zealot, target);

        ping(firstPyromancer, zealot);
        ping(secondPyromancer, zealot);

        assertThat(zealot.getMarkedDamage()).isZero();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void redirectExpiresAtEndOfTurn() {
        Permanent zealot = castFaceDown();
        Permanent target = addCreatureReady(player2, new HillGiant());
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());

        turnFaceUp(zealot, target);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        ping(pyromancer, zealot);

        assertThat(zealot.getMarkedDamage()).isEqualTo(1);
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void turningFaceUpOnlyAllowsCreatureTargets() {
        Permanent zealot = castFaceDown();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(zealot));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId())
                .doesNotContain(plains.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new KaronasZealot()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return findPermanent(player1, "Karona's Zealot");
    }

    private void turnFaceUp(Permanent zealot, Permanent target) {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(zealot));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    private void ping(Permanent pyromancer, Permanent target) {
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(pyromancer), null,
                target.getId());
        harness.passBothPriorities();
    }

}
