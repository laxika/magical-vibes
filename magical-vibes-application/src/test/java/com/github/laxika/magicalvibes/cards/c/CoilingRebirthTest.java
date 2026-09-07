package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CoilingRebirth.class, GrizzlyBears.class, HolyDay.class, IsamaruHoundOfKonda.class})
class CoilingRebirthTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature and creates a 1/1 copy when Gift is promised")
    void returnsCreatureAndCreatesCopyWhenGiftPromised() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new CoilingRebirth()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        harness.castSorceryWithGift(player1, 0, creature.getId(), true);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        Permanent token = gameData.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(gameData.playerHands.get(player2.getId())).hasSize(opponentHandSize + 1);
    }

    @Test
    @DisplayName("Returns a creature without creating a copy when Gift is not promised")
    void returnsCreatureWithoutCopyWhenGiftNotPromised() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new CoilingRebirth()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        harness.castSorceryWithGift(player1, 0, creature.getId(), false);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gameData.playerHands.get(player2.getId())).hasSize(opponentHandSize);
    }

    @Test
    @DisplayName("Does not copy the returned creature when it is legendary")
    void doesNotCopyLegendaryCreature() {
        Card creature = new IsamaruHoundOfKonda();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new CoilingRebirth()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorceryWithGift(player1, 0, creature.getId(), true);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Cannot target a noncreature card in a graveyard")
    void cannotTargetNoncreatureCard() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new CoilingRebirth()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorceryWithGift(player1, 0, instant.getId(), false))
                .isInstanceOf(IllegalStateException.class);
    }
}
