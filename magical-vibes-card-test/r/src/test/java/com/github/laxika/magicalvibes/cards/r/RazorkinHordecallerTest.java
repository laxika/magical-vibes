package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FearOfBeingHunted;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RazorkinHordecaller.class, FearOfBeingHunted.class})
class RazorkinHordecallerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a 1/1 red Gremlin token")
    void attackingCreatesGremlinToken() {
        addCreatureReady(player1, new RazorkinHordecaller());
        addCreatureReady(player1, new FearOfBeingHunted());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Gremlin");
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isAttackedThisTurn()).isFalse();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not create a token when you do not attack")
    void noTokenWhenNotAttacking() {
        addCreatureReady(player1, new RazorkinHordecaller());

        declareAttackers(List.of());

        assertThat(findPermanents(player1, "Gremlin")).isEmpty();
    }

    @Test
    @DisplayName("Multiple attackers create only one token per Hordecaller")
    void multipleAttackersCreateOneToken() {
        addCreatureReady(player1, new RazorkinHordecaller());
        addCreatureReady(player1, new FearOfBeingHunted());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Gremlin")).isEqualTo(1);
        assertThat(findPermanents(player2, "Gremlin")).isEmpty();
    }

    @Test
    @DisplayName("Each Hordecaller triggers independently when you attack")
    void multipleHordecallersEachCreateToken() {
        addCreatureReady(player1, new RazorkinHordecaller());
        addCreatureReady(player1, new RazorkinHordecaller());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Gremlin")).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's attack does not trigger your Hordecaller")
    void opponentAttackOnlyTriggersOpponentsHordecaller() {
        addCreatureReady(player1, new RazorkinHordecaller());
        addCreatureReady(player2, new RazorkinHordecaller());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Gremlin")).isEmpty();
        assertThat(countPermanents(player2, "Gremlin")).isEqualTo(1);
    }

    @Test
    @DisplayName("Haste allows Hordecaller to attack the turn it enters")
    void summoningSickHordecallerCanAttack() {
        harness.addToBattlefield(player1, new RazorkinHordecaller());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Gremlin")).isEqualTo(1);
    }

    @Test
    @DisplayName("The attack trigger still creates a token after Hordecaller leaves")
    void triggerResolvesAfterSourceLeaves() {
        Permanent hordecaller = addCreatureReady(player1, new RazorkinHordecaller());
        addCreatureReady(player1, new FearOfBeingHunted());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(1)));
        assertThat(findPermanents(player1, "Gremlin")).isEmpty();
        gd.playerBattlefields.get(player1.getId()).remove(hordecaller);
        gd.playerGraveyards.get(player1.getId()).add(hordecaller.getCard());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Gremlin")).isEqualTo(1);
    }

    @Test
    @DisplayName("The created token is a red Gremlin creature with no haste")
    void createdTokenHasCorrectCharacteristics() {
        addCreatureReady(player1, new RazorkinHordecaller());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Gremlin");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.GREMLIN);
        assertThat(token.getCard().getKeywords()).isEmpty();
        assertThat(token.isSummoningSick()).isTrue();
    }
}
