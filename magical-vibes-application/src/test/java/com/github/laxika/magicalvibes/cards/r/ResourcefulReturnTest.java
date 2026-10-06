package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ResourcefulReturn.class, GrizzlyBears.class, Forest.class, Ornithopter.class})
class ResourcefulReturnTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature card and draws when an artifact is controlled")
    void returnsCreatureAndDrawsWithArtifact() {
        Card creature = new GrizzlyBears();
        Card draw = new Forest();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(draw));
        harness.addToBattlefield(player1, new Ornithopter());
        harness.setHand(player1, List.of(new ResourcefulReturn()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .contains(creature.getId(), draw.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Returns a creature card without drawing when no artifact is controlled")
    void returnsCreatureWithoutDrawingWithoutArtifact() {
        Card creature = new GrizzlyBears();
        Card draw = new Forest();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(draw));
        harness.setHand(player1, List.of(new ResourcefulReturn()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

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
        harness.setHand(player1, List.of(new ResourcefulReturn()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetOpponentsCreature() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new ResourcefulReturn()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentsArtifactDoesNotEnableDraw() {
        Card creature = new GrizzlyBears();
        Card draw = new Forest();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(draw));
        harness.addToBattlefield(player2, new Ornithopter());
        harness.setHand(player1, List.of(new ResourcefulReturn()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(harness.getGameData().playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).containsExactly(draw);
    }

    @Test
    void artifactEnteringBeforeResolutionEnablesDraw() {
        Card creature = new GrizzlyBears();
        Card draw = new Forest();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(draw));
        harness.setHand(player1, List.of(new ResourcefulReturn()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, creature.getId());

        harness.addToBattlefield(player1, new Ornithopter());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(creature, draw);
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void artifactLeavingBeforeResolutionPreventsDraw() {
        Card creature = new GrizzlyBears();
        Card draw = new Forest();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(draw));
        harness.addToBattlefield(player1, new Ornithopter());
        harness.setHand(player1, List.of(new ResourcefulReturn()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, creature.getId());

        harness.getGameData().playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).containsExactly(draw);
    }

    @Test
    void illegalTargetPreventsDrawEvenWithArtifact() {
        Card creature = new GrizzlyBears();
        Card draw = new Forest();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(draw));
        harness.addToBattlefield(player1, new Ornithopter());
        harness.setHand(player1, List.of(new ResourcefulReturn()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, creature.getId());

        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).containsExactly(draw);
        harness.assertInGraveyard(player1, "Resourceful Return");
    }

    @Test
    void returningArtifactCreatureDoesNotEnableDraw() {
        Card creature = new Ornithopter();
        Card draw = new Forest();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(draw));
        harness.setHand(player1, List.of(new ResourcefulReturn()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(harness.getGameData().playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).containsExactly(draw);
    }
}
