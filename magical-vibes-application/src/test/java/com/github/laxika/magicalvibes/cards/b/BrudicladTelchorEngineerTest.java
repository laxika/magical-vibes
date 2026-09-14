package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BrudicladTelchorEngineer.class)
class BrudicladTelchorEngineerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Phyrexian Myr token and gives creature tokens haste")
    void createsMyrAndGrantsHaste() {
        harness.addToBattlefield(player1, new BrudicladTelchorEngineer());

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, true);

        Permanent myr = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(myr.getCard().getName()).isEqualTo("Phyrexian Myr");
        assertThat(myr.getCard().getPower()).isEqualTo(2);
        assertThat(myr.getCard().getToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, myr, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Copies every other controlled token, including noncreature tokens")
    void copiesOtherControlledTokens() {
        harness.addToBattlefield(player1, new BrudicladTelchorEngineer());
        Permanent chosen = addToken(player1, "Goblin", CardType.CREATURE, 1, 1,
                CardColor.RED, CardSubtype.GOBLIN);
        Permanent otherCreature = addToken(player1, "Dragon", CardType.CREATURE, 5, 5,
                CardColor.RED, CardSubtype.DRAGON);
        Permanent food = addToken(player1, "Food", CardType.ARTIFACT, 0, 0,
                null, CardSubtype.FOOD);
        Permanent opponentToken = addToken(player2, "Opponent Token", CardType.CREATURE, 7, 7,
                CardColor.GREEN, CardSubtype.BEAST);

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(chosen.getId(), otherCreature.getId(), food.getId())
                .doesNotContain(opponentToken.getId());
        harness.handlePermanentChosen(player1, chosen.getId());

        assertThat(chosen.getCard().getName()).isEqualTo("Goblin");
        assertThat(otherCreature.getCard().getName()).isEqualTo("Goblin");
        assertThat(otherCreature.getCard().getPower()).isEqualTo(1);
        assertThat(food.getCard().getName()).isEqualTo("Goblin");
        assertThat(food.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(opponentToken.getCard().getName()).isEqualTo("Opponent Token");
    }

    @Test
    @DisplayName("Declining the copy choice leaves existing tokens unchanged")
    void mayDeclineCopyChoice() {
        harness.addToBattlefield(player1, new BrudicladTelchorEngineer());
        Permanent token = addToken(player1, "Dragon", CardType.CREATURE, 5, 5,
                CardColor.RED, CardSubtype.DRAGON);

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(token.getCard().getName()).isEqualTo("Dragon");
        assertThat(token.getCard().getPower()).isEqualTo(5);
    }

    @Test
    @DisplayName("The temporary token copies revert at end of turn")
    void copiesRevertAtEndOfTurn() {
        harness.addToBattlefield(player1, new BrudicladTelchorEngineer());
        Permanent chosen = addToken(player1, "Goblin", CardType.CREATURE, 1, 1,
                CardColor.RED, CardSubtype.GOBLIN);
        Permanent other = addToken(player1, "Dragon", CardType.CREATURE, 5, 5,
                CardColor.RED, CardSubtype.DRAGON);

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, chosen.getId());
        assertThat(other.getCard().getName()).isEqualTo("Goblin");

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(other.getCard().getName()).isEqualTo("Dragon");
        assertThat(other.getCard().getPower()).isEqualTo(5);
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }

    private Permanent addToken(Player player, String name, CardType type, int power, int toughness,
                               CardColor color, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setAdditionalTypes(type == CardType.CREATURE ? Set.of() : Set.of());
        card.setColor(color);
        card.setSubtypes(List.of(subtype));
        card.setToken(true);
        if (type == CardType.CREATURE) {
            card.setPower(power);
            card.setToughness(toughness);
        }

        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
