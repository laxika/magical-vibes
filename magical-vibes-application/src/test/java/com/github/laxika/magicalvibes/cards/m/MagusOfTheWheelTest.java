package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagusOfTheWheel.class, SavannahLions.class})
class MagusOfTheWheelTest extends BaseCardTest {

    @Test
    @DisplayName("Each player discards their hand and draws seven cards")
    void eachPlayerDiscardsHandAndDrawsSeven() {
        setDeck(player1, 7);
        setDeck(player2, 7);
        harness.setHand(player1, List.of(new SavannahLions(), new SavannahLions()));
        harness.setHand(player2, List.of(new SavannahLions()));
        Permanent magus = addCreatureReady(player1, new MagusOfTheWheel());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(magus.getCard());
    }

    @Test
    @DisplayName("Empty hands still draw seven cards")
    void emptyHandsStillDrawSeven() {
        setDeck(player1, 7);
        setDeck(player2, 7);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        addCreatureReady(player1, new MagusOfTheWheel());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
    }

    @Test
    @DisplayName("Sacrifice is paid on activation before either hand changes")
    void sacrificeIsPaidBeforeResolution() {
        setDeck(player1, 7);
        setDeck(player2, 7);
        SavannahLions firstHandCard = new SavannahLions();
        SavannahLions secondHandCard = new SavannahLions();
        harness.setHand(player1, List.of(firstHandCard));
        harness.setHand(player2, List.of(secondHandCard));
        Permanent magus = addCreatureReady(player1, new MagusOfTheWheel());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(magus);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(magus.getCard());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstHandCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(secondHandCard);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7).doesNotContain(firstHandCard);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7).doesNotContain(secondHandCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstHandCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(secondHandCard);
    }

    @Test
    @DisplayName("A summoning sick Magus cannot activate its tap ability")
    void summoningSicknessPreventsActivation() {
        Permanent magus = harness.addToBattlefieldAndReturn(player1, new MagusOfTheWheel());
        magus.setSummoningSick(true);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(magus);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Magus cannot activate its tap ability")
    void tappedMagusCannotActivate() {
        Permanent magus = addCreatureReady(player1, new MagusOfTheWheel());
        magus.tap();
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(magus);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The activation cost requires red mana")
    void colorlessManaCannotPayRedCost() {
        Permanent magus = addCreatureReady(player1, new MagusOfTheWheel());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(magus);
        assertThat(magus.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void setDeck(Player player, int count) {
        harness.setLibrary(player, IntStream.range(0, count)
                .mapToObj(i -> new SavannahLions()).toList());
    }
}
