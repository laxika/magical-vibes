package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AncientCraving;
import com.github.laxika.magicalvibes.cards.t.TouchOfBrilliance;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AirElemental.class, AncientCraving.class, DJVu.class, TouchOfBrilliance.class})
class DJVuTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target sorcery card from your graveyard to your hand")
    void returnsSorceryFromGraveyardToHand() {
        Card sorcery = new AncientCraving();
        harness.setGraveyard(player1, List.of(sorcery));
        harness.setHand(player1, List.of(new DJVu()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0, sorcery.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(sorcery.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(sorcery.getId()));
    }

    @Test
    @DisplayName("Cannot target a non-sorcery card")
    void cannotTargetNonSorceryCard() {
        Card nonSorcery = new AirElemental();
        harness.setGraveyard(player1, List.of(nonSorcery));
        harness.setHand(player1, List.of(new DJVu()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, nonSorcery.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card sorcery = new AncientCraving();
        harness.setGraveyard(player2, List.of(sorcery));
        harness.setHand(player1, List.of(new DJVu()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, sorcery.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }

    @Test
    @DisplayName("Fizzles if target card leaves graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyard() {
        Card sorcery = new AncientCraving();
        harness.setGraveyard(player1, List.of(sorcery));
        harness.setHand(player1, List.of(new DJVu()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, sorcery.getId());
        harness.getGameData().playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Requires a sorcery card target in your graveyard")
    void requiresSorceryTarget() {
        harness.setHand(player1, List.of(new DJVu()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Returns only the targeted sorcery when several are in the graveyard")
    void returnsOnlyTargetedSorcery() {
        Card otherSorcery = new TouchOfBrilliance();
        Card targetSorcery = new TouchOfBrilliance();
        harness.setGraveyard(player1, List.of(otherSorcery, targetSorcery));
        harness.setHand(player1, List.of(new DJVu()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, targetSorcery.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(targetSorcery.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(otherSorcery.getId()))
                .noneMatch(c -> c.getId().equals(targetSorcery.getId()));
    }
}
