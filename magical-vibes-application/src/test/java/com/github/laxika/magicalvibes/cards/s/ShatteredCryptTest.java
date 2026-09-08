package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Abjure;
import com.github.laxika.magicalvibes.cards.b.BenalishInfantry;
import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShatteredCrypt.class, BenalishInfantry.class, BenalishKnight.class, Abjure.class})
class ShatteredCryptTest extends BaseCardTest {

    @Test
    @DisplayName("Only creature cards in your own graveyard are legal targets")
    void onlyOwnCreatureCardsAreTargetable() {
        Card infantry = new BenalishInfantry();
        Card knight = new BenalishKnight();
        Card nonCreature = new Abjure();
        Card opponentInfantry = new BenalishInfantry();
        harness.setGraveyard(player1, List.of(infantry, knight, nonCreature));
        harness.setGraveyard(player2, List.of(opponentInfantry));
        harness.setHand(player1, List.of(new ShatteredCrypt()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 2);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(infantry.getId(), knight.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returns X creature cards to hand and the controller loses X life")
    void returnsXCreaturesAndLosesXLife() {
        Card infantry = new BenalishInfantry();
        Card knight = new BenalishKnight();
        harness.setGraveyard(player1, List.of(infantry, knight));
        harness.setHand(player1, List.of(new ShatteredCrypt()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        int startingLife = gd.getLife(player1.getId());

        harness.castSorcery(player1, 0, 2);
        harness.handleMultipleCardsChosen(player1, List.of(infantry.getId(), knight.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Benalish Infantry");
        harness.assertInHand(player1, "Benalish Knight");
        harness.assertInGraveyard(player1, "Shattered Crypt");
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife - 2);
    }

    @Test
    @DisplayName("Fewer than X targets cannot be chosen")
    void mustChooseExactlyXTargets() {
        Card infantry = new BenalishInfantry();
        Card knight = new BenalishKnight();
        harness.setGraveyard(player1, List.of(infantry, knight));
        harness.setHand(player1, List.of(new ShatteredCrypt()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 2);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(infantry.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("X larger than the number of creature cards in your graveyard is an illegal cast")
    void cannotChooseXAboveAvailableCreatures() {
        harness.setGraveyard(player1, List.of(new BenalishInfantry()));
        harness.setHand(player1, List.of(new ShatteredCrypt()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("X=0 resolves with no returns and no life loss")
    void xZeroDoesNothing() {
        harness.setGraveyard(player1, List.of(new BenalishInfantry()));
        harness.setHand(player1, List.of(new ShatteredCrypt()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        int startingLife = gd.getLife(player1.getId());

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Benalish Infantry");
        harness.assertInGraveyard(player1, "Shattered Crypt");
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife);
    }
}
