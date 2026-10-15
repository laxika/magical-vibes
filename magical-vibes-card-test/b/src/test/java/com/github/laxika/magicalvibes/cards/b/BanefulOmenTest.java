package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GelatinousGenesis;
import com.github.laxika.magicalvibes.cards.n.NestInvader;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BanefulOmen.class, NestInvader.class, Swamp.class, GelatinousGenesis.class})
class BanefulOmenTest extends BaseCardTest {

    @Test
    @DisplayName("At your end step, revealing the top card makes each opponent lose its mana value")
    void revealsTopCardAndEachOpponentLosesItsManaValue() {
        Card topCard = new NestInvader();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new BanefulOmen());

        advanceToEndStep(player1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    @DisplayName("May ability can be declined")
    void mayAbilityCanBeDeclined() {
        Card topCard = new NestInvader();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new BanefulOmen());

        advanceToEndStep(player1);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    @DisplayName("Does not trigger on an opponent's end step")
    void doesNotTriggerOnOpponentsEndStep() {
        harness.setLibrary(player1, List.of(new NestInvader()));
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new BanefulOmen());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Revealing a land causes no life loss and leaves it on top")
    void revealingLandCausesNoLifeLoss() {
        Card topCard = new Swamp();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new BanefulOmen());

        advanceToEndStep(player1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("An empty library causes no life loss")
    void emptyLibraryCausesNoLifeLoss() {
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new BanefulOmen());

        advanceToEndStep(player1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The trigger still resolves after Baneful Omen leaves the battlefield")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        Card topCard = new NestInvader();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new BanefulOmen());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("The ability uses its controller's library and makes the other player lose life")
    void usesControllerLibraryWhenControlledBySecondPlayer() {
        harness.setLibrary(player1, List.of(new Swamp()));
        Card topCard = new NestInvader();
        harness.setLibrary(player2, List.of(topCard));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new BanefulOmen());

        advanceToEndStep(player2);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("X in a revealed card's mana cost counts as zero")
    void xInRevealedManaCostCountsAsZero() {
        Card topCard = new GelatinousGenesis();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new BanefulOmen());

        advanceToEndStep(player1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("The top card is determined when the ability resolves")
    void usesTopCardAtResolution() {
        harness.setLibrary(player1, List.of(new NestInvader()));
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new BanefulOmen());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        Card topCard = new BanefulOmen();
        harness.setLibrary(player1, List.of(topCard));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(13);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }
    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
