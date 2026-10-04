package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.d.Deduce;
import com.github.laxika.magicalvibes.cards.d.DemandAnswers;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlotsamJetsam.class, CounselOfTheSoratami.class, Island.class, Deduce.class, DemandAnswers.class})
class FlotsamJetsamTest extends BaseCardTest {

    @Test
    @DisplayName("Flotsam mills three cards and creates a Clue")
    void flotsamMillsAndInvestigates() {
        Island first = new Island();
        Island second = new Island();
        Island third = new Island();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new FlotsamJetsam()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(first, second, third)
                .hasSize(4);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Jetsam mills each opponent and casts one opponent-graveyard spell for free")
    void jetsamMillsAndCastsFromOpponentGraveyard() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        Island first = new Island();
        Island second = new Island();
        Island third = new Island();
        harness.setGraveyard(player2, List.of(counsel));
        harness.setLibrary(player2, List.of(first, second, third));
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new FlotsamJetsam()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castModalInstant(player1, 0, 1, List.of());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second, third);
        assertThat(gd.findExiledCard(counsel.getId()).card()).isSameAs(counsel);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void flotsamInvestigatesWithAnEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new FlotsamJetsam()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void jetsamCannotBeCastDuringCombat() {
        harness.setHand(player1, List.of(new FlotsamJetsam()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void jetsamCanCastANewlyMilledSpell() {
        Deduce deduce = new Deduce();
        Island first = new Island();
        Island second = new Island();
        harness.setLibrary(player2, List.of(deduce, first, second));
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new FlotsamJetsam()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second);
        assertThat(gd.findExiledCard(deduce.getId()).card()).isSameAs(deduce);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void jetsamMayDeclineCasting() {
        Deduce deduce = new Deduce();
        harness.setGraveyard(player2, List.of(deduce));
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new FlotsamJetsam()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(deduce);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void jetsamCannotCastWithoutPayingMandatoryAdditionalCost() {
        DemandAnswers demand = new DemandAnswers();
        harness.setGraveyard(player2, List.of(demand));
        harness.setLibrary(player2, List.of());
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.setHand(player1, List.of(new FlotsamJetsam()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(demand);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
