package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(HazardrootHerbalist.class)
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
        Permanent target = addCreatureReady(player1, createCreature("Grizzly Bears", 2, 2));

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
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
