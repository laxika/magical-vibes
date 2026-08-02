package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ObeliskOfUrdTest extends BaseCardTest {

    private static Card createCreature(String name, String manaCost, int power, int toughness,
                                       CardColor color, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        card.setSubtypes(List.of(subtypes));
        return card;
    }

    private Permanent putObeliskWithChosenType(CardSubtype subtype) {
        Permanent obelisk = new Permanent(new ObeliskOfUrd());
        obelisk.setChosenSubtype(subtype);
        gd.playerBattlefields.get(player1.getId()).add(obelisk);
        return obelisk;
    }

    @Test
    @DisplayName("Resolving Obelisk of Urd prompts for a creature type and stores the choice")
    void castingPromptsForSubtypeChoice() {
        harness.setHand(player1, List.of(new ObeliskOfUrd()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "GOBLIN");

        assertThat(findPermanent(player1, "Obelisk of Urd").getChosenSubtype()).isEqualTo(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("Creatures you control of the chosen type get +2/+2")
    void boostsOwnCreaturesOfChosenType() {
        harness.addToBattlefield(player1, createCreature("Goblin Piker", "{1}{R}", 2, 1, CardColor.RED, CardSubtype.GOBLIN));
        putObeliskWithChosenType(CardSubtype.GOBLIN);

        var bonus = gqs.computeStaticBonus(gd, findPermanent(player1, "Goblin Piker"));
        assertThat(bonus.power()).isEqualTo(2);
        assertThat(bonus.toughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures of a different type are not boosted")
    void doesNotBoostOtherTypes() {
        harness.addToBattlefield(player1, createCreature("Llanowar Elves", "{G}", 1, 1, CardColor.GREEN, CardSubtype.ELF));
        putObeliskWithChosenType(CardSubtype.GOBLIN);

        var bonus = gqs.computeStaticBonus(gd, findPermanent(player1, "Llanowar Elves"));
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("Opponent creatures of the chosen type are not boosted")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player2, createCreature("Goblin Piker", "{1}{R}", 2, 1, CardColor.RED, CardSubtype.GOBLIN));
        putObeliskWithChosenType(CardSubtype.GOBLIN);

        var bonus = gqs.computeStaticBonus(gd, findPermanent(player2, "Goblin Piker"));
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost disappears when Obelisk of Urd leaves the battlefield")
    void boostRemovedWhenObeliskLeaves() {
        harness.addToBattlefield(player1, createCreature("Goblin Piker", "{1}{R}", 2, 1, CardColor.RED, CardSubtype.GOBLIN));
        Permanent obelisk = putObeliskWithChosenType(CardSubtype.GOBLIN);

        Permanent goblin = findPermanent(player1, "Goblin Piker");
        assertThat(gqs.computeStaticBonus(gd, goblin).power()).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(obelisk);

        assertThat(gqs.computeStaticBonus(gd, goblin).power()).isEqualTo(0);
    }
}
