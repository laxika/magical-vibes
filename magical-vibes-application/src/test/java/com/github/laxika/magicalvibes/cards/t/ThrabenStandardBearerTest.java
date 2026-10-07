package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FaithbearerPaladin;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThrabenStandardBearer.class, FaithbearerPaladin.class})
class ThrabenStandardBearerTest extends BaseCardTest {

    @Test
    @DisplayName("{1}{W}, tap, and discard a card creates a 1/1 Human Soldier token")
    void activationCreatesHumanSoldier() {
        setupMainPhase();
        Permanent bearer = addCreatureReady(player1, new ThrabenStandardBearer());
        harness.setHand(player1, List.of(new FaithbearerPaladin()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(bearer.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Faithbearer Paladin");

        Permanent token = findPermanent(player1, "Human Soldier");
        assertThat(token.getCard().getName()).isEqualTo("Human Soldier");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.isTapped()).isFalse();
        assertThat(countPermanents(player1, "Human Soldier")).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Human Soldier");
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.HUMAN, CardSubtype.SOLDIER);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        setupMainPhase();
        addCreatureReady(player1, new ThrabenStandardBearer());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertNotOnBattlefield(player1, "Human Soldier");
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        setupMainPhase();
        Permanent bearer = addCreatureReady(player1, new ThrabenStandardBearer());
        bearer.tap();
        harness.setHand(player1, List.of(new FaithbearerPaladin()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithSummoningSickness() {
        setupMainPhase();
        Permanent bearer = harness.addToBattlefieldAndReturn(player1, new ThrabenStandardBearer());
        harness.setHand(player1, List.of(new FaithbearerPaladin()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bearer.isTapped()).isFalse();
        harness.assertNotInGraveyard(player1, "Faithbearer Paladin");
        harness.assertNotOnBattlefield(player1, "Human Soldier");
    }

    @Test
    void cannotActivateWithoutWhiteMana() {
        setupMainPhase();
        Permanent bearer = addCreatureReady(player1, new ThrabenStandardBearer());
        harness.setHand(player1, List.of(new FaithbearerPaladin()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bearer.isTapped()).isFalse();
        harness.assertNotInGraveyard(player1, "Faithbearer Paladin");
    }

    @Test
    void cannotActivateWithOnlyOneMana() {
        setupMainPhase();
        Permanent bearer = addCreatureReady(player1, new ThrabenStandardBearer());
        harness.setHand(player1, List.of(new FaithbearerPaladin()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bearer.isTapped()).isFalse();
        harness.assertNotInGraveyard(player1, "Faithbearer Paladin");
    }

    @Test
    void costsArePaidBeforeResolutionAndAbilitySurvivesSourceLeaving() {
        setupMainPhase();
        Permanent bearer = addCreatureReady(player1, new ThrabenStandardBearer());
        harness.setHand(player1, List.of(new FaithbearerPaladin(), new ThrabenStandardBearer()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 1);

        assertThat(bearer.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Thraben Standard Bearer");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertNotInGraveyard(player1, "Faithbearer Paladin");
        harness.assertNotOnBattlefield(player1, "Human Soldier");

        gd.playerBattlefields.get(player1.getId()).remove(bearer);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Human Soldier")).isEqualTo(1);
    }

    private void setupMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
