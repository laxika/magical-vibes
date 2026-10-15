package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BergStrider;
import com.github.laxika.magicalvibes.cards.d.DualStrike;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TundraFumarole.class, GarrukWildspeaker.class, Forest.class, BergStrider.class, DualStrike.class})
class TundraFumaroleTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage and adds persistent colorless mana for snow mana spent")
    void dealsDamageAndAddsPersistentManaForSnowManaSpent() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 7);
        harness.setHand(player1, List.of(new TundraFumarole()));

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        pool.addSnowMana(ManaColor.RED, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castAndResolveSorcery(player1, 0, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(3);

        gs.advanceStep(gd);

        assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("Adds no mana when no snow mana was spent")
    void addsNoManaWithoutSnowMana() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 7);
        harness.setHand(player1, List.of(new TundraFumarole()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castAndResolveSorcery(player1, 0, planeswalker.getId());

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(1);

        gs.advanceStep(gd);

        assertThat(pool.get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new TundraFumarole()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can kill your own creature and refunds all three snow mana")
    void killsOwnCreatureAndRefundsAllSnowManaUntilEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BergStrider());
        harness.setHand(player1, List.of(new TundraFumarole()));
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSnowMana(ManaColor.RED, 2);
        pool.addSnowMana(ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof BergStrider);
        assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(3);

        gs.advanceStep(gd);
        gs.advanceStep(gd);
        assertThat(pool.get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Adds no mana if the only target leaves before resolution")
    void addsNoManaWhenTargetLeavesBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BergStrider());
        harness.setHand(player1, List.of(new TundraFumarole()));
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSnowMana(ManaColor.RED, 3);

        harness.castSorcery(player1, 0, creature.getId());
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerHands.get(player2.getId()).add(creature.getCard());
        harness.passBothPriorities();

        assertThat(pool.get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof TundraFumarole);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new TundraFumarole()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A spell copy deals damage but does not refund the original snow payment")
    void copyDoesNotAddManaForOriginalSnowPayment() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 10);
        harness.setHand(player1, List.of(new DualStrike()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0);

        harness.setHand(player1, List.of(new TundraFumarole()));
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSnowMana(ManaColor.RED, 3);
        harness.castSorcery(player1, 0, planeswalker.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(3);
    }
}
