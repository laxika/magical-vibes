package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SigardaHeronsGraceTest extends BaseCardTest {

    @Test
    @DisplayName("Gives the controller and Humans they control hexproof")
    void givesControllerAndHumansHexproof() {
        harness.addToBattlefield(player1, new SigardaHeronsGrace());
        harness.addToBattlefield(player1, new EliteVanguard());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new EliteVanguard());

        Permanent human = findPermanent(player1, "Elite Vanguard");
        Permanent nonHuman = findPermanent(player1, "Grizzly Bears");
        Permanent opponentHuman = findPermanent(player2, "Elite Vanguard");

        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isTrue();
        assertThat(gqs.playerHasHexproof(gd, player2.getId())).isFalse();
        assertThat(gqs.hasKeyword(gd, human, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonHuman, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentHuman, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Exiles a graveyard card and creates a Human Soldier token")
    void exilesGraveyardCardAndCreatesToken() {
        harness.addToBattlefield(player1, new SigardaHeronsGrace());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertNotInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Shock"));

        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Human Soldier");
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes())
                .containsExactly(CardSubtype.HUMAN, CardSubtype.SOLDIER);
        assertThat(gqs.hasKeyword(gd, token, Keyword.HEXPROOF)).isTrue();
    }
}
