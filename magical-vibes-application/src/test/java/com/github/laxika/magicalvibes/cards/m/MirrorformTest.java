package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.f.FathomSeer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shapesharer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Mirrorform.class, DarksteelRelic.class, GrizzlyBears.class, HillGiant.class,
        Island.class, Pacifism.class, FathomSeer.class, Shapesharer.class})
class MirrorformTest extends BaseCardTest {

    private void castMirrorform(Permanent target) {
        harness.setHand(player1, List.of(new Mirrorform()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    @Test
    @DisplayName("Each nonland permanent you control becomes a copy of the target")
    void copiesControlledNonlandPermanents() {
        Permanent target = addCreatureReady(player1, new HillGiant());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        castMirrorform(target);

        assertThat(target.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(bears.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(bears.getCard().getPower()).isEqualTo(3);
        assertThat(bears.getCard().getToughness()).isEqualTo(3);
        assertThat(relic.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(island.getCard().getName()).isEqualTo("Island");
        assertThat(opponentBears.getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("The copy effect is permanent")
    void copiesDoNotWearOff() {
        Permanent target = addCreatureReady(player1, new HillGiant());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        castMirrorform(target);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getCard().getName()).isEqualTo("Hill Giant");
    }

    @Test
    @DisplayName("A non-Aura permanent is required as the target")
    void cannotTargetAura() {
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        harness.setHand(player1, List.of(new Mirrorform()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, aura.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canCopyOpponentPermanentWithoutCopyingTappedStatusOrCounters() {
        Permanent target = addCreatureReady(player2, new HillGiant());
        target.setTapped(true);
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castMirrorform(target);

        assertThat(bears.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(bears.isTapped()).isFalse();
        assertThat(target.isTapped()).isTrue();
        assertThat(bears.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void canTargetLandAndTurnNonlandsIntoLands() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());

        castMirrorform(target);

        assertThat(bears.getCard().getName()).isEqualTo("Island");
        assertThat(relic.getCard().getName()).isEqualTo("Island");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears, relic);
    }

    @Test
    void doesNothingWhenTargetLeavesBeforeResolution() {
        Permanent target = addCreatureReady(player2, new HillGiant());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Mirrorform()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(bears.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void controlledAuraAlsoBecomesCopy() {
        Permanent target = addCreatureReady(player2, new HillGiant());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        aura.setAttachedTo(bears.getId());

        castMirrorform(target);

        assertThat(aura.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
        assertThat(aura.getAttachedTo()).isNull();
    }

    @Test
    void copiesFaceDownCharacteristicsInsteadOfHiddenCard() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FathomSeer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        Permanent target = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isFaceDown).findFirst().orElseThrow();

        castMirrorform(target);

        assertThat(bears.isFaceDown()).isFalse();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(bears.getCard().getManaValue()).isZero();
    }

    @Test
    void permanentCopySurvivesExpirationOfEarlierTemporaryCopy() {
        Permanent shapesharer = addCreatureReady(player1, new Shapesharer());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent giant = addCreatureReady(player2, new HillGiant());
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(shapesharer.getId(), bears.getId()));
        harness.passBothPriorities();

        castMirrorform(giant);
        assertThat(shapesharer.getCard().getName()).isEqualTo("Hill Giant");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(shapesharer.getCard().getName()).isEqualTo("Hill Giant");
    }
}
