package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.ArmoredPegasus;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PegasusRefuge.class, ArmoredPegasus.class})
class PegasusRefugeTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2} and discarding a card creates a 1/1 white flying Pegasus token")
    void createsPegasusToken() {
        harness.addToBattlefield(player1, new PegasusRefuge());
        harness.setHand(player1, List.of(new ArmoredPegasus()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Armored Pegasus");
        List<Permanent> tokens = findPermanents(player1, "Pegasus");
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.PEGASUS);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(token.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Discards only the card chosen from a hand with multiple cards")
    void discardsOnlyChosenCard() {
        harness.addToBattlefield(player1, new PegasusRefuge());
        ArmoredPegasus kept = new ArmoredPegasus();
        ArmoredPegasus discarded = new ArmoredPegasus();
        harness.setHand(player1, List.of(kept, discarded));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(findPermanents(player1, "Pegasus")).hasSize(1);
    }

    @Test
        @DisplayName("Cannot activate with an empty hand")
    void cannotActivateWithEmptyHand() {
        harness.addToBattlefield(player1, new PegasusRefuge());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new PegasusRefuge());
        harness.setHand(player1, List.of(new ArmoredPegasus()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability can be activated repeatedly")
    void abilityIsRepeatable() {
        harness.addToBattlefield(player1, new PegasusRefuge());
        harness.setHand(player1, List.of(new ArmoredPegasus(), new ArmoredPegasus()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Pegasus")).hasSize(2);
    }

    @Test
    @DisplayName("Activation suspends on a discard-cost choice before the ability goes on the stack")
    void activationSuspendsOnDiscardChoice() {
        harness.addToBattlefield(player1, new PegasusRefuge());
        harness.setHand(player1, List.of(new ArmoredPegasus()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(gd.stack).isEmpty();
    }
}
