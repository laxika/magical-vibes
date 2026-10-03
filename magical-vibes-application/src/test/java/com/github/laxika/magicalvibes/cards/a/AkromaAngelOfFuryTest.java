package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DismalFailure;
import com.github.laxika.magicalvibes.cards.d.DustElemental;
import com.github.laxika.magicalvibes.cards.g.GiantDustwasp;
import com.github.laxika.magicalvibes.cards.k.KavuPredator;
import com.github.laxika.magicalvibes.cards.p.PiracyCharm;
import com.github.laxika.magicalvibes.cards.s.Sunlance;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AkromaAngelOfFury.class, DismalFailure.class, DustElemental.class, GiantDustwasp.class, KavuPredator.class,
        PiracyCharm.class, Sunlance.class})
class AkromaAngelOfFuryTest extends BaseCardTest {

    @Test
    void cannotBeCounteredByDismalFailure() {
        AkromaAngelOfFury akroma = new AkromaAngelOfFury();
        harness.setHand(player1, List.of(akroma));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.setHand(player2, List.of(new DismalFailure()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, akroma.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Akroma, Angel of Fury");
        harness.assertInGraveyard(player2, "Dismal Failure");
    }

    @Test
    void protectionFromWhiteAndBluePreventsTargeting() {
        Permanent akroma = harness.addToBattlefieldAndReturn(player2, new AkromaAngelOfFury());

        harness.setHand(player1, List.of(new PiracyCharm()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of(akroma.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new Sunlance()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, akroma.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new AkromaAngelOfFury());
        addCreatureReady(player2, new KavuPredator());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void trampleDealsExcessCombatDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new AkromaAngelOfFury());
        Permanent blocker = addCreatureReady(player2, new GiantDustwasp());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 3,
                player2.getId(), 3
        ));

        harness.assertLife(player2, 17);
        harness.assertOnBattlefield(player1, "Akroma, Angel of Fury");
        harness.assertInGraveyard(player2, "Giant Dustwasp");
    }

    @Test
    void redAbilityBoostsPowerUntilEndOfTurn() {
        Permanent akroma = harness.addToBattlefieldAndReturn(player1, new AkromaAngelOfFury());
        int basePower = gqs.getEffectivePower(gd, akroma);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, akroma)).isEqualTo(basePower + 1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, akroma)).isEqualTo(basePower);
    }

    @Test
    void canBeCastFaceDownAndTurnedFaceUpForMorphCost() {
        harness.setHand(player1, List.of(new AkromaAngelOfFury()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent akroma = findPermanent(player1, "Akroma, Angel of Fury");
        assertThat(akroma.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(akroma));
        harness.passBothPriorities();

        assertThat(akroma.isFaceDown()).isFalse();
    }

    @Test
    void faceDownSpellCanBeCountered() {
        AkromaAngelOfFury akroma = new AkromaAngelOfFury();
        harness.setHand(player1, List.of(akroma));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player2, List.of(new DismalFailure()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreatureWithMorph(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, akroma.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Akroma, Angel of Fury");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void protectionFromWhitePreventsFlyingWhiteBlocker() {
        addCreatureReady(player1, new AkromaAngelOfFury());
        addCreatureReady(player2, new DustElemental());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void redAbilityCanBeActivatedRepeatedlyWithoutTapping() {
        Permanent akroma = harness.addToBattlefieldAndReturn(player1, new AkromaAngelOfFury());
        int basePower = gqs.getEffectivePower(gd, akroma);
        int baseToughness = gqs.getEffectiveToughness(gd, akroma);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, akroma)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, akroma)).isEqualTo(baseToughness);
        assertThat(akroma.isTapped()).isFalse();
    }

    @Test
    void turningFaceUpInResponseToSunlanceGainsProtectionImmediately() {
        harness.setHand(player1, List.of(new AkromaAngelOfFury()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
        Permanent akroma = findPermanent(player1, "Akroma, Angel of Fury");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Sunlance()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castSorcery(player2, 0, akroma.getId());
        harness.passPriority(player2);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int stackSize = gd.stack.size();

        harness.turnFaceUp(player1, 0);

        assertThat(akroma.isFaceDown()).isFalse();
        assertThat(gd.stack).hasSize(stackSize);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Akroma, Angel of Fury");
        assertThat(akroma.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Sunlance");
    }
}
