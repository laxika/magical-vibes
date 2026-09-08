package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.Gravecrawler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CemeteryRecruitmentTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature card and draws when it is a Zombie")
    void returnsZombieAndDraws() {
        Card zombie = new Gravecrawler();
        Card draw = new Forest();
        harness.setGraveyard(player1, List.of(zombie));
        harness.setLibrary(player1, List.of(draw));
        harness.setHand(player1, List.of(new CemeteryRecruitment()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, zombie.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .contains(zombie.getId(), draw.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(zombie.getId()));
    }

    @Test
    @DisplayName("Returns a non-Zombie creature without drawing")
    void returnsNonZombieWithoutDrawing() {
        Card creature = new GrizzlyBears();
        Card draw = new Forest();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(draw));
        harness.setHand(player1, List.of(new CemeteryRecruitment()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .contains(creature.getId())
                .doesNotContain(draw.getId());
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .contains(draw.getId());
    }

    @Test
    @DisplayName("Cannot target a noncreature card in a graveyard")
    void cannotTargetNoncreatureCard() {
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        harness.setHand(player1, List.of(new CemeteryRecruitment()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
