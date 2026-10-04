package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BarkformHarvester;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HazardrootHerbalist.class, BarkformHarvester.class})
class HazardrootHerbalistTest extends BaseCardTest {

    @Test
    @DisplayName("Whenever you attack, a creature you control gets +1/+0")
    void boostsChosenCreatureYouControl() {
        addCreatureReady(player1, new HazardrootHerbalist());
        Permanent target = addCreatureReady(player1, createTokenCreature("Rabbit Token", 1, 1));
        Permanent opponentCreature = addCreatureReady(player2, createTokenCreature("Opponent Token", 1, 1));

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId()).doesNotContain(opponentCreature.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("The trigger occurs once per combat when multiple creatures attack")
    void triggersOncePerCombat() {
        Permanent herbalist = addCreatureReady(player1, new HazardrootHerbalist());
        Permanent firstAttacker = addCreatureReady(player1, createTokenCreature("First Token", 1, 1));
        Permanent secondAttacker = addCreatureReady(player1, createTokenCreature("Second Token", 1, 1));

        declareAttackers(List.of(1, 2));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                herbalist.getId(), firstAttacker.getId(), secondAttacker.getId());

        harness.handlePermanentChosen(player1, firstAttacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, firstAttacker))
                .isEqualTo(gqs.getEffectivePower(gd, secondAttacker) + 1);
    }

    @Test
    @DisplayName("Nontoken targets get no deathtouch")
    void nontokenTargetDoesNotGainDeathtouch() {
        addCreatureReady(player1, new HazardrootHerbalist());
        Permanent target = addCreatureReady(player1, new BarkformHarvester());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("The Herbalist may target itself without gaining deathtouch")
    void canTargetItself() {
        Permanent herbalist = addCreatureReady(player1, new HazardrootHerbalist());
        addCreatureReady(player1, new BarkformHarvester());
        int initialPower = gqs.getEffectivePower(gd, herbalist);
        int initialToughness = gqs.getEffectiveToughness(gd, herbalist);

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, herbalist.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, herbalist)).isEqualTo(initialPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, herbalist)).isEqualTo(initialToughness);
        assertThat(gqs.hasKeyword(gd, herbalist, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("An opponent attacking does not trigger the Herbalist")
    void doesNotTriggerForOpponentAttacks() {
        Permanent herbalist = addCreatureReady(player1, new HazardrootHerbalist());
        addCreatureReady(player2, new BarkformHarvester());
        int initialPower = gqs.getEffectivePower(gd, herbalist);

        declareAttackers(player2, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, herbalist)).isEqualTo(initialPower);
    }

    @Test
    @DisplayName("The attack trigger resolves even if the Herbalist leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent herbalist = addCreatureReady(player1, new HazardrootHerbalist());
        Permanent target = addCreatureReady(player1, new BarkformHarvester());
        int initialPower = gqs.getEffectivePower(gd, target);

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(herbalist);
        gd.playerGraveyards.get(player1.getId()).add(herbalist.getCard());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(initialPower + 1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Both the power bonus and token deathtouch expire at cleanup")
    void tokenBonusAndDeathtouchExpireAtCleanup() {
        addCreatureReady(player1, new HazardrootHerbalist());
        Card tokenCopy = new BarkformHarvester();
        tokenCopy.setToken(true);
        Permanent target = addCreatureReady(player1, tokenCopy);
        int initialPower = gqs.getEffectivePower(gd, target);
        int initialToughness = gqs.getEffectiveToughness(gd, target);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(initialPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(initialToughness);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(initialPower);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(initialToughness);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isFalse();
    }

    private Card createTokenCreature(String name, int power, int toughness) {
        Card card = createCreature(name, power, toughness);
        card.setToken(true);
        return card;
    }

    private Card createCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
