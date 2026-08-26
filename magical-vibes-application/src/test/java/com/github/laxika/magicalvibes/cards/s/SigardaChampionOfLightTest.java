package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SigardaChampionOfLight.class)
class SigardaChampionOfLightTest extends BaseCardTest {

    @Test
    @DisplayName("Humans you control get +1/+1")
    void boostsHumansYouControl() {
        Permanent human = addCreatureReady(player1, creature("Human", 3, 2, CardSubtype.HUMAN));
        Permanent nonHuman = addCreatureReady(player1, creature("Bear", 2, 2));
        int humanPower = gqs.getEffectivePower(gd, human);
        int humanToughness = gqs.getEffectiveToughness(gd, human);
        int nonHumanPower = gqs.getEffectivePower(gd, nonHuman);
        int nonHumanToughness = gqs.getEffectiveToughness(gd, nonHuman);

        harness.addToBattlefield(player1, new SigardaChampionOfLight());

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(humanPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(humanToughness + 1);
        assertThat(gqs.getEffectivePower(gd, nonHuman)).isEqualTo(nonHumanPower);
        assertThat(gqs.getEffectiveToughness(gd, nonHuman)).isEqualTo(nonHumanToughness);
    }

    @Test
    @DisplayName("Coven attack trigger offers a Human creature from the top five")
    void covenAttackTriggerOffersHumanCreature() {
        Permanent sigarda = addCreatureReady(player1, new SigardaChampionOfLight());
        addCreatureReady(player1, creature("One", 1, 1));
        addCreatureReady(player1, creature("Two", 2, 2));
        addCreatureReady(player1, creature("Three", 3, 3));

        Card human = creature("Library Human", 2, 2, CardSubtype.HUMAN);
        Card nonCreature = card("Noncreature", CardType.INSTANT);
        harness.setLibrary(player1, List.of(human, nonCreature, creature("Four", 4, 4),
                creature("Five", 5, 5), card("Another Noncreature", CardType.SORCERY)));

        declareAttackers(List.of(battlefieldIndex(sigarda)));
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(human.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(human.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(human);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The attack trigger does not happen without three different powers")
    void attackTriggerRequiresCoven() {
        Permanent sigarda = addCreatureReady(player1, new SigardaChampionOfLight());
        addCreatureReady(player1, creature("One", 2, 2));
        addCreatureReady(player1, creature("Two", 2, 2));
        Card human = creature("Library Human", 2, 2, CardSubtype.HUMAN);
        harness.setLibrary(player1, List.of(human, card("Noncreature One", CardType.INSTANT),
                creature("Three", 3, 3), creature("Four", 4, 4), card("Noncreature Two", CardType.SORCERY)));

        declareAttackers(List.of(battlefieldIndex(sigarda)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(human);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    private Card creature(String name, int power, int toughness, CardSubtype... subtypes) {
        Card card = card(name, CardType.CREATURE);
        card.setPower(power);
        card.setToughness(toughness);
        card.setSubtypes(List.of(subtypes));
        return card;
    }

    private Card card(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        return card;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
