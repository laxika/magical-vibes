package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DiregrafHorde;
import com.github.laxika.magicalvibes.cards.i.InheritedFiend;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeirloomMirror.class, InheritedFiend.class, DiregrafHorde.class, Island.class})
class HeirloomMirrorTest extends BaseCardTest {

    @Test
    void activationDrawsMillsAndAddsRitualCounter() {
        Permanent mirror = addMirrorReady(player1);
        Card discarded = new DiregrafHorde();
        Card drawn = new DiregrafHorde();
        Card milled = new DiregrafHorde();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn, milled));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(player1, mirror), null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded, milled);
        assertThat(mirror.getCounterCount(CounterType.RITUAL)).isEqualTo(1);
    }

    @Test
    void transformsAfterTheThirdRitualCounter() {
        Permanent mirror = addMirrorReady(player1);
        harness.setHand(player1, List.of(new DiregrafHorde(), new DiregrafHorde(), new DiregrafHorde()));
        harness.setLibrary(player1, List.of(new DiregrafHorde(), new DiregrafHorde(),
                new DiregrafHorde(), new DiregrafHorde(), new DiregrafHorde(), new DiregrafHorde()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, indexOf(player1, mirror), null, null);
            harness.handleCardChosen(player1, 0);
            harness.passBothPriorities();
            if (i < 2) {
                mirror.untap();
            }
        }

        assertThat(mirror.isTransformed()).isTrue();
        assertThat(mirror.getCounterCount(CounterType.RITUAL)).isZero();
    }

    @Test
    void backAbilityExilesCreatureAndAddsPlusOnePlusOneCounter() {
        Permanent fiend = addTransformedMirror(player1);
        Card target = new DiregrafHorde();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, indexOf(player1, fiend), null, target.getId(), com.github.laxika.magicalvibes.model.Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
        assertThat(fiend.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void backAbilityCannotTargetNoncreatureCard() {
        Permanent fiend = addTransformedMirror(player1);
        Card target = new Island();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, fiend), null, target.getId(), com.github.laxika.magicalvibes.model.Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void frontAbilityCannotBeActivatedOutsideMainPhase() {
        Permanent mirror = addMirrorReady(player1);
        harness.setHand(player1, List.of(new DiregrafHorde()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.END_STEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, mirror), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mirror.isTapped()).isFalse();
        harness.assertLife(player1, 20);
    }

    @Test
    void frontAbilityStillDrawsAndMillsAfterMirrorLeavesBattlefield() {
        Permanent mirror = addMirrorReady(player1);
        Card discarded = new DiregrafHorde();
        Card drawn = new DiregrafHorde();
        Card milled = new DiregrafHorde();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn, milled));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, indexOf(player1, mirror), null, null);
        harness.handleCardChosen(player1, 0);
        gd.playerBattlefields.get(player1.getId()).remove(mirror);
        harness.setGraveyard(player1, List.of(discarded, mirror.getCard()));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milled);
        assertThat(mirror.getCounterCount(CounterType.RITUAL)).isZero();
    }

    @Test
    void backAbilityDoesNotAddCounterWhenTargetLeavesGraveyard() {
        Permanent fiend = addTransformedMirror(player1);
        Card target = new DiregrafHorde();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, indexOf(player1, fiend), null, target.getId(),
                com.github.laxika.magicalvibes.model.Zone.GRAVEYARD);
        harness.setGraveyard(player2, List.of());
        harness.setHand(player2, List.of(target));

        harness.passBothPriorities();

        assertThat(fiend.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void backAbilityCanExileFromOwnGraveyardAfterFiendLeavesBattlefield() {
        Permanent fiend = addTransformedMirror(player1);
        Card target = new DiregrafHorde();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, indexOf(player1, fiend), null, target.getId(),
                com.github.laxika.magicalvibes.model.Zone.GRAVEYARD);
        gd.playerBattlefields.get(player1.getId()).remove(fiend);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(target);
        assertThat(fiend.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void frontAbilityRequiresACardToDiscard() {
        Permanent mirror = addMirrorReady(player1);
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, mirror), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mirror.isTapped()).isFalse();
        harness.assertLife(player1, 20);
    }

    @Test
    void transformationRemovesAllRitualCountersButPreservesOtherCounters() {
        Permanent mirror = addMirrorReady(player1);
        mirror.setCounterCount(CounterType.RITUAL, 4);
        mirror.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new DiregrafHorde()));
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(player1, mirror), null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(mirror.isTransformed()).isTrue();
        assertThat(mirror.getCounterCount(CounterType.RITUAL)).isZero();
        assertThat(mirror.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void backAbilityCanBeActivatedOutsideMainPhaseWhileSummoningSickAndTapped() {
        Permanent fiend = addTransformedMirror(player1);
        fiend.setSummoningSick(true);
        fiend.tap();
        harness.forceStep(TurnStep.END_STEP);
        Card target = new DiregrafHorde();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, indexOf(player1, fiend), null, target.getId(),
                com.github.laxika.magicalvibes.model.Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(target);
        assertThat(fiend.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addMirrorReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new HeirloomMirror());
    }

    private Permanent addTransformedMirror(Player player) {
        HeirloomMirror card = new HeirloomMirror();
        Permanent mirror = harness.addToBattlefieldAndReturn(player, card);
        mirror.setCard(card.getBackFaceCard());
        mirror.setTransformed(true);
        return mirror;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
