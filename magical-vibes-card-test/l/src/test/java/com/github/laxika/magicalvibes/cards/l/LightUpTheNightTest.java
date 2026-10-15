package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CandlegroveWitch;
import com.github.laxika.magicalvibes.cards.t.TeferiWhoSlowsTheSunset;
import com.github.laxika.magicalvibes.cards.w.WrennAndSeven;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LightUpTheNight.class, CandlegroveWitch.class, WrennAndSeven.class, TeferiWhoSlowsTheSunset.class})
class LightUpTheNightTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to a player")
    void dealsXDamageToPlayer() {
        harness.setHand(player1, List.of(new LightUpTheNight()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 2, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals X plus 1 damage to a creature or planeswalker")
    void dealsExtraDamageToPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CandlegroveWitch());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new WrennAndSeven());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new LightUpTheNight(), new LightUpTheNight()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 2, creature.getId());
        harness.castAndResolveSorcery(player1, 0, 2, planeswalker.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature.getCard());
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Flashback removes X loyalty counters from planeswalkers and exiles the spell")
    void flashbackRemovesLoyaltyCounters() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new WrennAndSeven());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        Card spell = new LightUpTheNight();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player2, 20);

        harness.castFlashbackWithCounterCost(player1, 0, 2, player2.getId(),
                List.of(planeswalker.getId(), planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("Flashback cannot choose zero for X")
    void flashbackRejectsZeroX() {
        Card spell = new LightUpTheNight();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFlashbackWithCounterCost(
                player1, 0, 0, player2.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
    }

    @Test
    void normalCastWithZeroXStillDealsOneDamageToCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CandlegroveWitch());
        harness.setHand(player1, List.of(new LightUpTheNight()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0, creature.getId());

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    void normalCastWithZeroXDealsNoDamageToPlayer() {
        harness.setHand(player1, List.of(new LightUpTheNight()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 0, player2.getId());

        harness.assertLife(player2, 20);
    }

    @Test
    void flashbackCanSplitLoyaltyCostAndDealBonusDamageToPlaneswalker() {
        Permanent wrenn = harness.enterBattlefieldAndReturn(player1, new WrennAndSeven());
        Permanent teferi = harness.enterBattlefieldAndReturn(player1, new TeferiWhoSlowsTheSunset());
        Permanent target = harness.enterBattlefieldAndReturn(player2, new WrennAndSeven());
        Card spell = new LightUpTheNight();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashbackWithCounterCost(player1, 0, 3, target.getId(),
                List.of(wrenn.getId(), wrenn.getId(), teferi.getId()));

        assertThat(wrenn.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    void flashbackMayRemoveAllLoyaltyCounters() {
        Permanent planeswalker = harness.enterBattlefieldAndReturn(player1, new WrennAndSeven());
        Card spell = new LightUpTheNight();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player2, 20);

        harness.castFlashbackWithCounterCost(player1, 0, 5, player2.getId(),
                List.of(planeswalker.getId(), planeswalker.getId(), planeswalker.getId(),
                        planeswalker.getId(), planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(planeswalker.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(planeswalker);
        harness.assertLife(player2, 15);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    void flashbackCannotRemoveOpponentsLoyaltyCounters() {
        Permanent planeswalker = harness.enterBattlefieldAndReturn(player2, new WrennAndSeven());
        Card spell = new LightUpTheNight();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFlashbackWithCounterCost(
                player1, 0, 1, player2.getId(), List.of(planeswalker.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
    }

    @Test
    void flashbackCannotRemoveMoreLoyaltyThanAvailable() {
        Permanent planeswalker = harness.enterBattlefieldAndReturn(player1, new WrennAndSeven());
        planeswalker.setCounterCount(CounterType.LOYALTY, 1);
        Card spell = new LightUpTheNight();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFlashbackWithCounterCost(
                player1, 0, 2, player2.getId(), List.of(planeswalker.getId(), planeswalker.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
    }
}
