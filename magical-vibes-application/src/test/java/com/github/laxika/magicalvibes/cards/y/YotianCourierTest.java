package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YotianCourier.class, Bonesplitter.class})
class YotianCourierTest extends BaseCardTest {

    private static final String POWERSTONE_MODE = "Create a tapped Powerstone token.";
    private static final String SEEK_MODE =
            "Seek a nonland card with mana value equal to the number of Powerstones you control.";

    @Test
    @DisplayName("The first attack can create a tapped Powerstone")
    void createsTappedPowerstone() {
        addReadyCourier();

        declareAttack();
        harness.handleListChoice(player1, POWERSTONE_MODE);
        harness.passBothPriorities();

        Permanent powerstone = findPermanents(player1, "Powerstone").getFirst();
        assertThat(powerstone.isTapped()).isTrue();
        assertThat(powerstone.getCard().getSubtypes()).contains(CardSubtype.POWERSTONE);
    }

    @Test
    @DisplayName("The next combat offers the other mode and seeks an exact Powerstone-count card")
    void nextCombatOffersOtherModeAndSeeksExactManaValue() {
        Permanent courier = addReadyCourier();
        Bonesplitter sought = new Bonesplitter();
        harness.setLibrary(player1, List.of(sought));

        declareAttack();
        harness.handleListChoice(player1, POWERSTONE_MODE);
        harness.passBothPriorities();

        courier.untap();
        declareAttack();
        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly(SEEK_MODE);

        harness.handleListChoice(player1, SEEK_MODE);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(sought);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private Permanent addReadyCourier() {
        Permanent courier = harness.addToBattlefieldAndReturn(player1, new YotianCourier());
        courier.setSummoningSick(false);
        return courier;
    }

    private void declareAttack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
    }
}
