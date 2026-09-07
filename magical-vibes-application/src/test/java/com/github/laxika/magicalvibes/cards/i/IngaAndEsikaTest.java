package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IngaAndEsika.class, GrizzlyBears.class})
class IngaAndEsikaTest extends BaseCardTest {

    @Test
    void creaturesGainVigilanceAndCreatureSpellOnlyManaAbility() {
        harness.addToBattlefield(player1, new IngaAndEsika());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(createPriorityHoldingCreature()));

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();

        activateGrantedManaAbility(creature);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GREEN");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isZero();
        assertThat(pool.getCreatureSpellOnlyMana(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void drawsWhenThreeCreatureManaWasSpentOnCreatureSpell() {
        harness.addToBattlefield(player1, new IngaAndEsika());
        harness.setHand(player1, List.of(createPriorityHoldingCreature()));
        activateGrantedManaAbility(addCreatureReady(player1, new GrizzlyBears()));
        harness.handleListChoice(player1, "GREEN");
        activateGrantedManaAbility(addCreatureReady(player1, createCreature("Source Two")));
        harness.handleListChoice(player1, "GREEN");
        activateGrantedManaAbility(addCreatureReady(player1, createCreature("Source Three")));
        harness.handleListChoice(player1, "GREEN");

        Card spell = createCreature("Three Mana Creature");
        harness.setHand(player1, List.of(spell));
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotDrawWhenFewerThanThreeCreatureManaWasSpent() {
        harness.addToBattlefield(player1, new IngaAndEsika());
        harness.setHand(player1, List.of(createPriorityHoldingCreature()));
        activateGrantedManaAbility(addCreatureReady(player1, new GrizzlyBears()));
        harness.handleListChoice(player1, "GREEN");
        activateGrantedManaAbility(addCreatureReady(player1, createCreature("Source Two")));
        harness.handleListChoice(player1, "GREEN");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Card spell = createCreature("Three Mana Creature");
        harness.setHand(player1, List.of(spell));
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void activateGrantedManaAbility(Permanent creature) {
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(creature);
        harness.activateAbility(player1, index, null, null);
    }

    private static Card createCreature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{3}");
        card.setColor(CardColor.GREEN);
        card.setPower(3);
        card.setToughness(3);
        return card;
    }

    private static Card createPriorityHoldingCreature() {
        Card card = createCreature("Priority Holder");
        card.setManaCost("{G}");
        return card;
    }
}
