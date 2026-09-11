package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GildedLotus;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KamiOfIndustry.class, MindStone.class, GildedLotus.class})
class KamiOfIndustryTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a small artifact with haste and sacrifices it at the next end step")
    void returnsSmallArtifactWithHasteAndSacrificesItAtNextEndStep() {
        Card artifact = new MindStone();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new KamiOfIndustry()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(artifact.getId());
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Mind Stone");
        assertThat(returned.getGrantedKeywords()).contains(Keyword.HASTE);
        harness.assertNotInGraveyard(player1, "Mind Stone");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mind Stone");
        harness.assertInGraveyard(player1, "Mind Stone");
    }

    @Test
    @DisplayName("Cannot target an artifact with mana value greater than three")
    void cannotTargetArtifactWithManaValueGreaterThanThree() {
        Card artifact = new GildedLotus();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new KamiOfIndustry()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNull();
        harness.assertInGraveyard(player1, "Gilded Lotus");
        harness.assertNotOnBattlefield(player1, "Gilded Lotus");
    }
}
