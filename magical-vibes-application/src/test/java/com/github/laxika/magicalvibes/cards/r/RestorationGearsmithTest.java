package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RestorationGearsmithTest extends BaseCardTest {

    @Test
    @DisplayName("ETB targets an artifact or creature card and returns it to hand")
    void etbReturnsArtifactOrCreatureToHand() {
        Card artifact = new Bonesplitter();
        Card creature = new GrizzlyBears();
        Card instant = new Shock();
        harness.setGraveyard(player1, List.of(instant, artifact, creature));

        castRestorationGearsmith();

        PendingInteraction.MultiGraveyardChoice choice =
                (PendingInteraction.MultiGraveyardChoice) gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).containsExactly(artifact.getId(), creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Bonesplitter");
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("ETB cannot target a card that is neither an artifact nor a creature")
    void etbCannotTargetOtherCardTypes() {
        harness.setGraveyard(player1, List.of(new Shock()));

        castRestorationGearsmith();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Shock");
    }

    private void castRestorationGearsmith() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new RestorationGearsmith()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
