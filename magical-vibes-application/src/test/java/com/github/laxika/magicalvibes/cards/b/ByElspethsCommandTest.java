package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ByElspethsCommand.class)
class ByElspethsCommandTest extends BaseCardTest {

    private static final String TARGET_SOLDIER =
            "Up to one target Soldier perpetually gets +1/+1 and gains flying.";
    private static final String HAND_SOLDIER =
            "Choose a Soldier card in your hand. It perpetually gets +1/+1 and gains vigilance.";
    private static final String TOKEN = "Create a 1/1 colorless Soldier artifact creature token.";

    @Test
    @DisplayName("Beginning of combat can perpetually buff an opponent's Soldier")
    void buffsAnyTargetSoldier() {
        harness.addToBattlefield(player1, new ByElspethsCommand());
        Permanent soldier = harness.addToBattlefieldAndReturn(player2,
                soldier("Opponent Soldier", 2, 2));
        Permanent nonSoldier = harness.addToBattlefieldAndReturn(player2,
                creature("Opponent Bear", 2, 2));

        advanceToCombat(player1);
        harness.handleListChoice(player1, TARGET_SOLDIER);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(soldier.getId()).doesNotContain(nonSoldier.getId());
        harness.handlePermanentChosen(player1, soldier.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, soldier, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The hand mode perpetually modifies a Soldier card")
    void buffsSoldierCardInHand() {
        harness.addToBattlefield(player1, new ByElspethsCommand());
        Card soldier = soldier("Hand Soldier", 2, 2);
        harness.setHand(player1, List.of(soldier));

        advanceToCombat(player1);
        harness.handleListChoice(player1, HAND_SOLDIER);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PerpetualCreatureCardChoice.class);
        harness.handleCardChosen(player1, 0);

        Card modified = gd.playerHands.get(player1.getId()).getFirst();
        assertThat(modified.getPower()).isEqualTo(3);
        assertThat(modified.getToughness()).isEqualTo(3);
        assertThat(modified.getKeywords()).contains(Keyword.VIGILANCE);
    }

    @Test
    @DisplayName("The token mode creates a colorless artifact Soldier")
    void createsColorlessArtifactSoldier() {
        harness.addToBattlefield(player1, new ByElspethsCommand());

        advanceToCombat(player1);
        harness.handleListChoice(player1, TOKEN);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Soldier");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("A mode chosen during one combat is unavailable during another combat that turn")
    void modesAreOncePerTurn() {
        harness.addToBattlefield(player1, new ByElspethsCommand());
        harness.setHand(player1, List.of(soldier("Hand Soldier", 2, 2)));

        advanceToCombat(player1);
        harness.handleListChoice(player1, TOKEN);
        harness.passBothPriorities();

        advanceToCombat(player1);
        assertThatThrownBy(() -> harness.handleListChoice(player1, TOKEN))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleListChoice(player1, HAND_SOLDIER);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Card soldier(String name, int power, int toughness) {
        Card card = creature(name, power, toughness);
        card.setSubtypes(List.of(CardSubtype.SOLDIER));
        return card;
    }

    private Card creature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
