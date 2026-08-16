package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GadrakTheCrownScourgeTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack unless controller controls four artifacts")
    void cannotAttackWithoutFourArtifacts() {
        addCreatureReady(player1, new GadrakTheCrownScourge());
        addArtifacts(player1, 3);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack when controller controls four artifacts")
    void canAttackWithFourArtifacts() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new GadrakTheCrownScourge());
        addArtifacts(player1, 4);

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("Creates one Treasure for each nontoken creature that died this turn")
    void createsTreasureForNontokenCreatureDeaths() {
        harness.addToBattlefield(player1, new GadrakTheCrownScourge());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent token = addCreatureReady(player1, createTokenCreature());

        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
        harness.castInstant(player2, 0, token.getId());
        harness.passBothPriorities();

        advanceToEndStep();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    private void addArtifacts(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new GolemsHeart());
        }
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Card createTokenCreature() {
        Card card = new Card();
        card.setName("Soldier Token");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
