package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BloodOnTheSnowTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all creatures and returns an eligible creature from the graveyard")
    void destroysCreaturesAndReturnsEligibleCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GloriousAnthem());
        LlanowarElves eligible = new LlanowarElves();
        HillGiant tooExpensive = new HillGiant();
        harness.setGraveyard(player1, List.of(eligible, tooExpensive));
        harness.setHand(player1, List.of(new BloodOnTheSnow()));
        addBloodMana(1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1,
                gd.playerGraveyards.get(player1.getId()).indexOf(eligible));

        harness.assertOnBattlefield(player1, "Glorious Anthem");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not return a card with mana value above the snow mana spent")
    void doesNotReturnTooExpensiveCard() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        HillGiant tooExpensive = new HillGiant();
        harness.setGraveyard(player1, List.of(tooExpensive));
        harness.setHand(player1, List.of(new BloodOnTheSnow()));
        addBloodMana(1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The planeswalker mode leaves creatures and can return a planeswalker")
    void destroysPlaneswalkersAndReturnsEligiblePlaneswalker() {
        Permanent battlefieldPlaneswalker = harness.addToBattlefieldAndReturn(player1, testPlaneswalker());
        battlefieldPlaneswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card eligible = testPlaneswalker();
        harness.setGraveyard(player1, List.of(eligible));
        harness.setHand(player1, List.of(new BloodOnTheSnow()));
        addBloodMana(4);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1,
                gd.playerGraveyards.get(player1.getId()).indexOf(eligible));

        harness.assertOnBattlefield(player1, "Test Planeswalker");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private Card testPlaneswalker() {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        card.setManaCost("{3}");
        card.setLoyalty(3);
        return card;
    }

    private void addBloodMana(int snowMana) {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.add(ManaColor.BLACK, 2);
        pool.addSnowMana(ManaColor.COLORLESS, snowMana);
    }
}
