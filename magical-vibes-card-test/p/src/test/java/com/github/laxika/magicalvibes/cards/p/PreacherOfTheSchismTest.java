package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.q.QuintoriusKand;
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

@CardUsed({PreacherOfTheSchism.class, QuintoriusKand.class})
class PreacherOfTheSchismTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a lifelink Vampire and draws while both players are tied for most life")
    void triggersForTiedLifeLeaders() {
        harness.setLibrary(player1, List.of(new PreacherOfTheSchism()));
        harness.setHand(player1, List.of());
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
        harness.setHand(player1, List.of());
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
        harness.setLibrary(player1, List.of(new PreacherOfTheSchism()));
        harness.setHand(player1, List.of());
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
    @DisplayName("Draws and loses life but creates no token when attacking a planeswalker while tied for most life")
    void drawsAgainstPlaneswalkerWhenControllerLeads() {
        harness.setLibrary(player1, List.of(new PreacherOfTheSchism()));
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addPreacherReady(player1);
        Permanent planeswalker = addPlaneswalker(player2);

        declareAttackers(player1, List.of(0), Map.of(0, planeswalker.getId()));

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(findPermanents(player1, "Vampire").stream()
                .filter(permanent -> permanent.getCard().isToken())).isEmpty();
    }

    @Test
    @DisplayName("A nonattacking Preacher does not trigger for another Preacher's attack")
    void nonattackingPreacherDoesNotTrigger() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        addPreacherReady(player1);
        addPreacherReady(player1);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Vampire").stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Each attacking Preacher triggers only for its own attack")
    void twoAttackingPreachersCreateTwoTokens() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        addPreacherReady(player1);
        addPreacherReady(player1);

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Vampire").stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(2);
    }

    @Test
    @DisplayName("Token creation and drawing are two separate triggered abilities at tied life")
    void tiedLifeCreatesSeparateTriggers() {
        harness.setLibrary(player1, List.of(new PreacherOfTheSchism()));
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addPreacherReady(player1);

        declareAttackers(List.of(0));

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        long tokenCount = findPermanents(player1, "Vampire").stream()
                .filter(permanent -> permanent.getCard().isToken()).count();
        int handSize = gd.playerHands.get(player1.getId()).size();
        assertThat(tokenCount + handSize).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20 - handSize);

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Vampire").stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Life changes after the attack do not remove either triggered ability")
    void lifeTotalsAreCheckedOnlyAtAttack() {
        harness.setLibrary(player1, List.of(new PreacherOfTheSchism()));
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addPreacherReady(player1);

        declareAttackers(List.of(0));
        harness.setLife(player1, 10);
        harness.setLife(player2, 30);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Vampire").stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(9);
    }

    @Test
    @DisplayName("The token trigger resolves even if the attacked player no longer has the most life")
    void tokenTriggerSurvivesChangeOfLifeLeader() {
        harness.setHand(player1, List.of());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        addPreacherReady(player1);

        declareAttackers(List.of(0));
        harness.setLife(player1, 30);
        harness.setLife(player2, 10);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Vampire").stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(30);
    }

    @Test
    @DisplayName("Attacking a planeswalker while behind in life triggers neither ability")
    void noTriggersAgainstPlaneswalkerWhenControllerIsBehind() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        addPreacherReady(player1);
        Permanent planeswalker = addPlaneswalker(player2);

        declareAttackers(player1, List.of(0), Map.of(0, planeswalker.getId()));

        assertThat(gd.stack).isEmpty();
    }

    private Permanent addPreacherReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new PreacherOfTheSchism());
        permanent.setSummoningSick(false);
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
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new QuintoriusKand());
        permanent.setCounterCount(CounterType.LOYALTY, 4);
        return permanent;
    }
}
