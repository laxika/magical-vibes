package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.ArchiveHaunt;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OverwhelmedArchivist.class, ArchiveHaunt.class, GrizzlyBears.class, Island.class})
class OverwhelmedArchivistTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a draw-then-discard trigger")
    void entersDrawsThenDiscards() {
        Card drawn = new Island();
        Card discarded = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(discarded, new OverwhelmedArchivist()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getId)).contains(drawn.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()).stream().map(Card::getId))
                .contains(discarded.getId());
    }

    @Test
    @DisplayName("Disturb enters transformed and the back face loots when it attacks")
    void disturbEntersTransformedAndAttacksWithLoot() {
        Card drawn = new Island();
        Card discarded = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(discarded));
        harness.setGraveyard(player1, List.of(new OverwhelmedArchivist()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        Permanent haunt = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(haunt.isTransformed()).isTrue();
        haunt.setSummoningSick(false);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getId)).contains(drawn.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()).stream().map(Card::getId))
                .contains(discarded.getId());
    }

    @Test
    @DisplayName("The transformed back face is exiled instead of going to a graveyard")
    void transformedBackFaceIsExiledInsteadOfGraveyard() {
        harness.setGraveyard(player1, List.of(new OverwhelmedArchivist()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        Permanent haunt = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, haunt));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId()))
                .contains(haunt.getOriginalCard().getId());
    }
}
