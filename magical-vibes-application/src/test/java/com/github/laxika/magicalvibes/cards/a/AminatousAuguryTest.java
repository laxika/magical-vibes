package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AminatousAugury.class, Forest.class, GrizzlyBears.class, PropheticPrism.class})
class AminatousAuguryTest extends BaseCardTest {

    @Test
    @DisplayName("Puts an optional land onto the battlefield, then offers one spell per type")
    void choosesLandAndOneSpellPerType() {
        Forest forest = new Forest();
        GrizzlyBears firstBears = new GrizzlyBears();
        GrizzlyBears secondBears = new GrizzlyBears();
        PropheticPrism prism = new PropheticPrism();
        cast(List.of(forest, firstBears, secondBears, prism));

        assertThat(activeChoice().offeredCardType()).isEqualTo(CardType.LAND);
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        assertThat(harness.getPermanentId(player1, "Forest")).isNotNull();

        PendingInteraction.AminatousAuguryChoice creatureChoice = activeChoice();
        assertThat(creatureChoice.offeredCardType())
                .isEqualTo(CardType.CREATURE);
        assertThat(creatureChoice.validCardIds()).containsExactlyInAnyOrder(
                firstBears.getId(), secondBears.getId());
        harness.handleMultipleCardsChosen(player1, List.of(firstBears.getId()));

        assertThat(activeChoice().offeredCardType())
                .isEqualTo(CardType.ARTIFACT);
        harness.handleMultipleCardsChosen(player1, List.of(prism.getId()));

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == firstBears);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == prism);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(secondBears);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    @DisplayName("Declining every Augury choice leaves the exiled cards in exile")
    void decliningChoicesLeavesCardsExiled() {
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        PropheticPrism prism = new PropheticPrism();
        cast(List.of(forest, bears, prism));

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(forest, bears, prism);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    private PendingInteraction.AminatousAuguryChoice activeChoice() {
        return (PendingInteraction.AminatousAuguryChoice) gd.interaction.activeInteraction();
    }

    private void cast(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new AminatousAugury()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
