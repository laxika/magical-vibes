package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SeshiroTheAnointedTest extends BaseCardTest {

    private static Card createCreature(String name, int power, int toughness, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        card.setSubtypes(List.of(subtypes));
        return card;
    }

    private Permanent addSeshiro(Player player) {
        Permanent permanent = new Permanent(new SeshiroTheAnointed());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyCreature(Player player, String name, CardSubtype... subtypes) {
        harness.addToBattlefield(player, createCreature(name, 1, 1, subtypes));
        Permanent permanent = findPermanent(player, name);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void runCombatDamage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage → trigger onto the stack
        harness.passBothPriorities(); // resolve the trigger (may prompt)
    }

    private int handSize(Player player) {
        return gd.playerHands.get(player.getId()).size();
    }

    @Test
    @DisplayName("Other Snakes you control get +2/+2")
    void boostsOtherOwnSnakes() {
        harness.addToBattlefield(player1, createCreature("Sakura Snake", 1, 1, CardSubtype.SNAKE));
        addSeshiro(player1);

        var bonus = gqs.computeStaticBonus(gd, findPermanent(player1, "Sakura Snake"));

        assertThat(bonus.power()).isEqualTo(2);
        assertThat(bonus.toughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Seshiro does not boost himself")
    void doesNotBoostSelf() {
        Permanent seshiro = addSeshiro(player1);

        assertThat(gqs.computeStaticBonus(gd, seshiro).power()).isZero();
    }

    @Test
    @DisplayName("Non-Snake creatures you control are not boosted")
    void doesNotBoostNonSnakes() {
        harness.addToBattlefield(player1, createCreature("Plain Beast", 2, 2, CardSubtype.BEAST));
        addSeshiro(player1);

        assertThat(gqs.computeStaticBonus(gd, findPermanent(player1, "Plain Beast")).power()).isZero();
    }

    @Test
    @DisplayName("Snakes an opponent controls are not boosted")
    void doesNotBoostOpponentSnakes() {
        harness.addToBattlefield(player2, createCreature("Foe Snake", 1, 1, CardSubtype.SNAKE));
        addSeshiro(player1);

        assertThat(gqs.computeStaticBonus(gd, findPermanent(player2, "Foe Snake")).power()).isZero();
    }

    @Test
    @DisplayName("Accepting the may ability draws a card when a Snake deals combat damage")
    void snakeCombatDamageDrawsCard() {
        addSeshiro(player1);
        addReadyCreature(player1, "Sakura Snake", CardSubtype.SNAKE).setAttacking(true);
        int before = handSize(player1);

        runCombatDamage();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(handSize(player1)).isEqualTo(before + 1);
    }

    @Test
    @DisplayName("Declining the may ability draws no card")
    void decliningDrawsNothing() {
        addSeshiro(player1);
        addReadyCreature(player1, "Sakura Snake", CardSubtype.SNAKE).setAttacking(true);
        int before = handSize(player1);

        runCombatDamage();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(handSize(player1)).isEqualTo(before);
    }

    @Test
    @DisplayName("A non-Snake dealing combat damage does not trigger the draw")
    void nonSnakeDoesNotTrigger() {
        addSeshiro(player1);
        addReadyCreature(player1, "Plain Beast", CardSubtype.BEAST).setAttacking(true);
        int before = handSize(player1);

        runCombatDamage();

        assertThat(handSize(player1)).isEqualTo(before);
    }

    @Test
    @DisplayName("Seshiro triggers for himself when he deals combat damage")
    void triggersForItself() {
        addSeshiro(player1).setAttacking(true);
        int before = handSize(player1);

        runCombatDamage();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(handSize(player1)).isEqualTo(before + 1);
    }
}
