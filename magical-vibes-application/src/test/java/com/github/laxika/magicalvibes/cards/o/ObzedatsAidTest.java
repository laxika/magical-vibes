package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObzedatsAidTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target creature card from your graveyard to the battlefield")
    void returnsCreatureFromGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new ObzedatsAid()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Returns a noncreature permanent card such as an artifact")
    void returnsArtifactFromGraveyard() {
        Card artifact = new IcyManipulator();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new ObzedatsAid()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, artifact.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(artifact.getId()));
    }

    @Test
    @DisplayName("Cannot target a nonpermanent card")
    void cannotTargetInstant() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new ObzedatsAid()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new ObzedatsAid()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if the target leaves the graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new ObzedatsAid()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, creature.getId());
        GameData gd = harness.getGameData();
        gd.playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(creature.getId()));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }
}
