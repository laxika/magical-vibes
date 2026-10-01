package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.p.PurgingStormbrood;
import com.github.laxika.magicalvibes.cards.r.RunescaleStormbrood;
import com.github.laxika.magicalvibes.cards.t.TwinmawStormbrood;
import com.github.laxika.magicalvibes.cards.w.WhirlwingStormbrood;
import com.github.laxika.magicalvibes.cards.d.DisruptiveStormbrood;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonweaveTapestry.class, PurgingStormbrood.class,
        TwinmawStormbrood.class, RunescaleStormbrood.class,
        DisruptiveStormbrood.class, WhirlwingStormbrood.class,
        GrizzlyBears.class, DirgurIslandDragon.class})
class DragonweaveTapestryTest extends BaseCardTest {

    @Test
    void entersAndConjuresTwoCopiesOfEachSpellbookCard() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new DragonweaveTapestry()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId()))
                .hasSize(11)
                .extracting(Card::getName)
                .containsOnlyOnce("Grizzly Bears")
                .containsOnly("Grizzly Bears", "Purging Stormbrood", "Twinmaw Stormbrood",
                        "Runescale Stormbrood", "Disruptive Stormbrood", "Whirlwing Stormbrood");
        assertThat(gd.playerDecks.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Purging Stormbrood"))
                .hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Twinmaw Stormbrood"))
                .hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Runescale Stormbrood"))
                .hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Disruptive Stormbrood"))
                .hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Whirlwing Stormbrood"))
                .hasSize(2);
    }

    @Test
    void drawsWhenYouCastADragonOrOmenSpell() {
        harness.addToBattlefield(player1, new DragonweaveTapestry());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(createDragon()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Grizzly Bears");
    }

    @Test
    void drawsWhenYouCastAnOmenSpell() {
        harness.addToBattlefield(player1, new DragonweaveTapestry());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new DirgurIslandDragon()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Grizzly Bears");
    }

    @Test
    void tappingAddsAChosenColor() {
        harness.addToBattlefield(player1, new DragonweaveTapestry());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    private static Card createDragon() {
        Card card = new Card();
        card.setName("Test Dragon");
        card.setType(CardType.CREATURE);
        card.setManaCost("{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(CardSubtype.DRAGON));
        return card;
    }
}
