package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FieryTemper;
import com.github.laxika.magicalvibes.cards.t.ThrabenInspector;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SigardaHeronsGrace.class, EliteVanguard.class, GrizzlyBears.class, Shock.class,
        ThrabenInspector.class, FieryTemper.class})
class SigardaHeronsGraceTest extends BaseCardTest {

    @Test
    @DisplayName("Gives the controller and Humans they control hexproof")
    void givesControllerAndHumansHexproof() {
        harness.addToBattlefield(player1, new SigardaHeronsGrace());
        Permanent human = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        Permanent nonHuman = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentHuman = harness.addToBattlefieldAndReturn(player2, new EliteVanguard());

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

    @Test
    @DisplayName("Cannot use an opponent's graveyard to pay the activation cost")
    void cannotExileFromOpponentsGraveyard() {
        harness.addToBattlefield(player1, new SigardaHeronsGrace());
        harness.setGraveyard(player2, List.of(new ThrabenInspector()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(countPermanents(player1, "Human Soldier")).isZero();
        harness.assertInGraveyard(player2, "Thraben Inspector");
    }

    @Test
    @DisplayName("A tapped Sigarda can exile a creature card without tapping again")
    void tappedSigardaCanExileCreatureCard() {
        Permanent sigarda = harness.addToBattlefieldAndReturn(player1, new SigardaHeronsGrace());
        sigarda.tap();
        harness.setGraveyard(player1, List.of(new ThrabenInspector()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Thraben Inspector");
        assertThat(countPermanents(player1, "Human Soldier")).isEqualTo(1);
        assertThat(sigarda.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The controller can target their own protected Human")
    void controllerCanTargetOwnHuman() {
        harness.addToBattlefield(player1, new SigardaHeronsGrace());
        Permanent human = harness.addToBattlefieldAndReturn(player1, new ThrabenInspector());
        harness.setHand(player1, List.of(new FieryTemper()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, human.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Thraben Inspector");
    }

    @Test
    @DisplayName("An opponent cannot target a protected Human")
    void opponentCannotTargetHuman() {
        harness.addToBattlefield(player1, new SigardaHeronsGrace());
        Permanent human = harness.addToBattlefieldAndReturn(player1, new ThrabenInspector());
        harness.setHand(player2, List.of(new FieryTemper()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, human.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent cannot target Sigarda's controller")
    void opponentCannotTargetController() {
        harness.addToBattlefield(player1, new SigardaHeronsGrace());
        harness.setHand(player2, List.of(new FieryTemper()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The token ability resolves after Sigarda dies, but her protection ends")
    void abilityResolvesAfterSigardaLeaves() {
        Permanent sigarda = harness.addToBattlefieldAndReturn(player1, new SigardaHeronsGrace());
        Permanent human = harness.addToBattlefieldAndReturn(player1, new ThrabenInspector());
        harness.setHand(player2, List.of(new FieryTemper(), new FieryTemper()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.RED, 4);
        harness.castInstant(player2, 0, sigarda.getId());
        harness.passBothPriorities();

        harness.setGraveyard(player1, List.of(new ThrabenInspector()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.castInstant(player2, 0, sigarda.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sigarda, Heron's Grace");
        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isFalse();
        assertThat(gqs.hasKeyword(gd, human, Keyword.HEXPROOF)).isFalse();

        resolveAllTriggers();

        assertThat(countPermanents(player1, "Human Soldier")).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Human Soldier"), Keyword.HEXPROOF)).isFalse();
    }
}
