package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.y.YotianSoldier;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MyrelShieldOfArgive.class, LlanowarElves.class, Shock.class, YotianSoldier.class})
class MyrelShieldOfArgiveTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates Soldier artifact creature tokens equal to Soldiers controlled")
    void attackingCreatesSoldierTokens() {
        addCreatureReady(player1, new MyrelShieldOfArgive());
        addCreatureReady(player1, new YotianSoldier());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        List<Permanent> tokens = findPermanents(player1, "Soldier");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isNull();
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
            assertThat(token.getCard().isToken()).isTrue();
        });
    }

    @Test
    @DisplayName("Opponents cannot cast spells or activate permanent abilities during Myrel's controller's turn")
    void restrictsOpponentsDuringControllerTurn() {
        Permanent myrel = addCreatureReady(player1, new MyrelShieldOfArgive());
        addCreatureReady(player2, new LlanowarElves());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, myrel.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThatThrownBy(() -> harness.tapPermanent(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your opponent's turn");
    }

    @Test
    @DisplayName("The restriction does not apply during an opponent's turn")
    void allowsOpponentsDuringTheirTurn() {
        Permanent myrel = addCreatureReady(player1, new MyrelShieldOfArgive());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, myrel.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Only the controller's Soldiers count, including Myrel herself")
    void doesNotCountOpponentsSoldiers() {
        addCreatureReady(player1, new MyrelShieldOfArgive());
        addCreatureReady(player2, new MyrelShieldOfArgive());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(1);
        assertThat(countPermanents(player2, "Soldier")).isZero();
    }

    @Test
    @DisplayName("The attack trigger counts Soldiers at resolution even if Myrel has left")
    void createsNoTokensWhenNoSoldiersRemain() {
        Permanent myrel = addCreatureReady(player1, new MyrelShieldOfArgive());

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(myrel);
        gd.playerGraveyards.get(player1.getId()).add(myrel.getCard());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Soldier")).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Soldier tokens from a previous attack count on the next attack")
    void countsPreviouslyCreatedSoldierTokens() {
        addCreatureReady(player1, new MyrelShieldOfArgive());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Soldier")).isEqualTo(1);

        harness.performUntapStep(player1);
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(3);
        assertThat(findPermanents(player1, "Soldier")).allSatisfy(token ->
                assertThat(token.isAttacking()).isFalse());
    }

    @Test
    @DisplayName("Opponents can activate creature mana abilities during their own turn")
    void allowsOpponentManaAbilitiesDuringTheirTurn() {
        addCreatureReady(player1, new MyrelShieldOfArgive());
        Permanent elves = addCreatureReady(player2, new LlanowarElves());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.tapPermanent(player2, 0);

        assertThat(elves.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId())
                .get(com.github.laxika.magicalvibes.model.ManaColor.GREEN)).isEqualTo(1);
    }
}
