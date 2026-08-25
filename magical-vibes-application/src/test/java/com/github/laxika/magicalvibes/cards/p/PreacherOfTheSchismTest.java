package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(PreacherOfTheSchism.class)
class PreacherOfTheSchismTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a lifelink Vampire and draws while both players are tied for most life")
    void triggersForTiedLifeLeaders() {
        harness.setLibrary(player1, List.of(new Card()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addPreacherReady(player1);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        Permanent token = findPermanents(player1, "Vampire").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.LIFELINK)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Creates only the Vampire when attacking the highest-life player while behind")
    void onlyCreatesTokenWhenAttackedPlayerLeads() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        addPreacherReady(player1);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Vampire").stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Draws and loses life without creating a token when attacking a lower-life player")
    void onlyDrawsWhenControllerLeads() {
        harness.setLibrary(player1, List.of(new Card()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);
        addPreacherReady(player1);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Vampire").stream()
                .filter(permanent -> permanent.getCard().isToken())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not trigger when attacking a planeswalker")
    void doesNotTriggerAgainstPlaneswalker() {
        harness.setLibrary(player1, List.of(new Card()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addPreacherReady(player1);
        Permanent planeswalker = addPlaneswalker(player2);

        declareAttackers(player1, List.of(0), Map.of(0, planeswalker.getId()));

        assertThat(gd.stack).isEmpty();
    }

    private Permanent addPreacherReady(Player player) {
        Permanent permanent = new Permanent(new PreacherOfTheSchism());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, Map<Integer, UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }

    private Permanent addPlaneswalker(Player player) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        card.setColor(CardColor.BLUE);
        card.setLoyalty(3);
        Permanent permanent = new Permanent(card);
        permanent.setCounterCount(CounterType.LOYALTY, 3);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
