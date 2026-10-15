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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrudicladTelchorEngineer.class})
class BrudicladTelchorEngineerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Phyrexian Myr token and gives creature tokens haste")
    void createsMyrAndGrantsHaste() {
        harness.addToBattlefield(player1, new BrudicladTelchorEngineer());

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, true);

        Permanent myr = findPermanent(player1, "Phyrexian Myr");
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
    @DisplayName("Token copies remain copies after cleanup")
    void copiesRemainAfterCleanup() {
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

        assertThat(other.getCard().getName()).isEqualTo("Goblin");
        assertThat(other.getCard().getPower()).isEqualTo(1);
    }

    @Test
    @DisplayName("A token copy of Brudiclad grants itself haste")
    void tokenBrudicladHasHaste() {
        BrudicladTelchorEngineer card = new BrudicladTelchorEngineer();
        card.setToken(true);
        Permanent brudiclad = harness.addToBattlefieldAndReturn(player1, card);

        assertThat(gqs.hasKeyword(gd, brudiclad, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Haste applies only to controlled creature tokens and ends when Brudiclad leaves")
    void hasteScopeAndSourceRemoval() {
        Permanent brudiclad = harness.addToBattlefieldAndReturn(player1, new BrudicladTelchorEngineer());
        Permanent ownToken = addToken(player1, "Goblin", CardType.CREATURE, 1, 1,
                CardColor.RED, CardSubtype.GOBLIN);
        Permanent opponentToken = addToken(player2, "Goblin", CardType.CREATURE, 1, 1,
                CardColor.RED, CardSubtype.GOBLIN);
        Permanent food = addToken(player1, "Food", CardType.ARTIFACT, 0, 0,
                null, CardSubtype.FOOD);

        assertThat(gqs.hasKeyword(gd, ownToken, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentToken, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, brudiclad, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, food, Keyword.HASTE)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(brudiclad);
        assertThat(gqs.hasKeyword(gd, ownToken, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Brudiclad does not trigger during the opponent's combat")
    void doesNotTriggerOnOpponentsTurn() {
        harness.addToBattlefield(player1, new BrudicladTelchorEngineer());

        advanceToCombat(player2);

        assertThat(countPermanents(player1, "Phyrexian Myr")).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The newly created Myr can be chosen to copy")
    void canChooseNewMyr() {
        harness.addToBattlefield(player1, new BrudicladTelchorEngineer());
        Permanent goblin = addToken(player1, "Goblin", CardType.CREATURE, 1, 1,
                CardColor.RED, CardSubtype.GOBLIN);

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, true);
        Permanent myr = findPermanent(player1, "Phyrexian Myr");
        harness.handlePermanentChosen(player1, myr.getId());

        assertThat(goblin.getCard().getName()).isEqualTo("Phyrexian Myr");
        assertThat(goblin.getCard().getPower()).isEqualTo(2);
        assertThat(goblin.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Choosing a noncreature token turns the new Myr into a noncreature too")
    void canChooseNoncreatureToken() {
        harness.addToBattlefield(player1, new BrudicladTelchorEngineer());
        Permanent food = addToken(player1, "Food", CardType.ARTIFACT, 0, 0,
                null, CardSubtype.FOOD);

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, true);
        Permanent myr = findPermanent(player1, "Phyrexian Myr");
        harness.handlePermanentChosen(player1, food.getId());

        assertThat(myr.getCard().getName()).isEqualTo("Food");
        assertThat(myr.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(myr.getCard().getAdditionalTypes()).doesNotContain(CardType.CREATURE);
        assertThat(gqs.hasKeyword(gd, myr, Keyword.HASTE)).isFalse();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();
    }

    private Permanent addToken(Player player, String name, CardType type, int power, int toughness,
                               CardColor color, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setColor(color);
        card.setSubtypes(List.of(subtype));
        card.setToken(true);
        if (type == CardType.CREATURE) {
            card.setPower(power);
            card.setToughness(toughness);
        }

        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
