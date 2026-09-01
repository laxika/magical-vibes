package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KitsaOtterballElite.class, CounselOfTheSoratami.class, GrizzlyBears.class, Island.class})
class KitsaOtterballEliteTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability draws a card, then prompts for a discard")
    void drawsThenDiscards() {
        addReadyKitsa();
        setDeck(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Copies a target instant or sorcery spell when Kitsa has enough power")
    void copiesOwnSpellAtPowerThreshold() {
        Permanent kitsa = addReadyKitsa();
        kitsa.setPowerModifier(2);

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.activateAbility(player1, 0, 1, null, counsel.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
    }

    @Test
    @DisplayName("Cannot copy a spell while Kitsa has less than three power")
    void cannotCopyBelowPowerThreshold() {
        addReadyKitsa();

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, counsel.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power must be 3 or greater");
    }

    private Permanent addReadyKitsa() {
        Permanent kitsa = harness.addToBattlefieldAndReturn(player1, new KitsaOtterballElite());
        kitsa.setSummoningSick(false);
        return kitsa;
    }

    private void setDeck(com.github.laxika.magicalvibes.model.Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
