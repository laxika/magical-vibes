package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagdaTheHoardmaster.class, Shock.class})
class MagdaTheHoardmasterTest extends BaseCardTest {

    @Test
    @DisplayName("Crime creates one tapped Treasure and triggers only once each turn")
    void crimeCreatesOneTappedTreasureOncePerTurn() {
        harness.addToBattlefield(player1, new MagdaTheHoardmaster());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(findPermanent(player1, "Treasure").isTapped()).isTrue();

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing three Treasures creates a hasty flying Scorpion Dragon")
    void sacrificingThreeTreasuresCreatesScorpionDragon() {
        harness.addToBattlefield(player1, new MagdaTheHoardmaster());
        harness.addToBattlefield(player1, createTreasureToken());
        harness.addToBattlefield(player1, createTreasureToken());
        harness.addToBattlefield(player1, createTreasureToken());

        harness.activateAbility(player1, 0, null, null);
        assertThat(countPermanents(player1, "Treasure")).isZero();

        harness.passBothPriorities();

        Permanent dragon = findPermanent(player1, "Scorpion Dragon");
        assertThat(dragon.getCard().getPower()).isEqualTo(4);
        assertThat(dragon.getCard().getToughness()).isEqualTo(4);
        assertThat(dragon.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(dragon.getCard().getSubtypes()).contains(CardSubtype.DRAGON);
        assertThat(dragon.getCard().getKeywords()).contains(Keyword.FLYING, Keyword.HASTE);
    }

    @Test
    @DisplayName("The ability cannot be activated without three Treasures")
    void cannotActivateWithoutThreeTreasures() {
        harness.addToBattlefield(player1, new MagdaTheHoardmaster());
        harness.addToBattlefield(player1, createTreasureToken());
        harness.addToBattlefield(player1, createTreasureToken());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    private Card createTreasureToken() {
        Card card = new Card();
        card.setName("Treasure");
        card.setType(CardType.ARTIFACT);
        card.setSubtypes(List.of(CardSubtype.TREASURE));
        return card;
    }
}
