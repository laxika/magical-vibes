package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AwakenedSkyclave;
import com.github.laxika.magicalvibes.cards.b.BoundByMoonsilver;
import com.github.laxika.magicalvibes.cards.d.DualShot;
import com.github.laxika.magicalvibes.cards.m.MagnifyingGlass;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InexorableBlob.class, Forest.class, DualShot.class, MagnifyingGlass.class,
        BoundByMoonsilver.class, InvasionOfZendikar.class, AwakenedSkyclave.class})
class InexorableBlobTest extends BaseCardTest {

    @Test
    void createsTappedAndAttackingOozeWithDelirium() {
        Permanent blob = addReadyBlob();
        setDelirium();

        declareAttack(blob);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);

        List<Permanent> oozes = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Ooze"))
                .toList();
        assertThat(oozes).hasSize(1);
        assertThat(oozes.getFirst().isTapped()).isTrue();
        assertThat(oozes.getFirst().isAttacking()).isTrue();
        assertThat(oozes.getFirst().isAttackedThisTurn()).isFalse();
        assertThat(oozes.getFirst().getAttackTarget()).isEqualTo(player2.getId());
        assertThat(oozes.getFirst().getCard().getPower()).isEqualTo(3);
        assertThat(oozes.getFirst().getCard().getToughness()).isEqualTo(3);
    }

    @Test
    void doesNotCreateOozeWithoutDelirium() {
        Permanent blob = addReadyBlob();
        harness.setGraveyard(player1, List.of(new Forest(), new DualShot(), new MagnifyingGlass()));

        declareAttack(blob);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Ooze"));
    }

    @Test
    void rechecksDeliriumWhenAttackTriggerResolves() {
        Permanent blob = addReadyBlob();
        setDelirium();

        declareAttack(blob);
        gd.playerGraveyards.get(player1.getId()).removeLast();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Ooze"));
    }

    private Permanent addReadyBlob() {
        Permanent blob = harness.addToBattlefieldAndReturn(player1, new InexorableBlob());
        blob.setSummoningSick(false);
        return blob;
    }

    @Test
    void gainingDeliriumAfterAttackingDoesNotCreateTrigger() {
        Permanent blob = addReadyBlob();
        harness.setGraveyard(player1, List.of(new Forest(), new DualShot(), new MagnifyingGlass()));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttack(blob));

        assertThat(gd.stack).isEmpty();
        setDelirium();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void opponentsGraveyardDoesNotSupplyDelirium() {
        Permanent blob = addReadyBlob();
        harness.setGraveyard(player1, List.of(new Forest(), new DualShot(), new MagnifyingGlass()));
        harness.setGraveyard(player2, List.of(new BoundByMoonsilver()));

        declareAttack(blob);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void fourCardsOfOnlyThreeTypesDoNotEnableDelirium() {
        Permanent blob = addReadyBlob();
        harness.setGraveyard(player1, List.of(
                new Forest(), new Forest(), new DualShot(), new MagnifyingGlass()));

        declareAttack(blob);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @CardUsed({InvasionOfZendikar.class, AwakenedSkyclave.class})
    void oozeCanAttackBattleProtectedByDefendingPlayer() {
        Permanent blob = addReadyBlob();
        setDelirium();
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfZendikar());
        battle.setProtectorPlayerId(player2.getId());
        battle.setCounterCount(CounterType.DEFENSE, 3);

        declareAttack(blob);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).as("The Ooze's controller may choose the protected battle").isNotNull();
        assertThat(choice.validIds()).contains(battle.getId());
    }

    private void declareAttack(Permanent blob) {
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(blob)));
    }

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new Forest(), new DualShot(), new MagnifyingGlass(), new BoundByMoonsilver()));
    }
}
