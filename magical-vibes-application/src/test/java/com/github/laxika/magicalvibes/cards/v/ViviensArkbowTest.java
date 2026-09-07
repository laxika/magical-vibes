package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ViviensArkbow.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class ViviensArkbowTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a creature with mana value at most X from the top X cards onto the battlefield")
    void putsEligibleCreatureOntoBattlefield() {
        Card discarded = new Forest();
        GrizzlyBears eligible = new GrizzlyBears();
        HillGiant tooExpensive = new HillGiant();
        harness.addToBattlefield(player1, new ViviensArkbow());
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(eligible, tooExpensive));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(tooExpensive);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Puts all looked-at cards on the bottom when no creature qualifies")
    void noEligibleCreatureLeavesCardsOnBottom() {
        Card discarded = new Forest();
        HillGiant creatureOverLimit = new HillGiant();
        Forest land = new Forest();
        harness.addToBattlefield(player1, new ViviensArkbow());
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(creatureOverLimit, land));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof HillGiant);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(creatureOverLimit, land);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
