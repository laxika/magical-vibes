package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FetchQuest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SeekThrills;
import com.github.laxika.magicalvibes.cards.b.BeanstalkWurm;
import com.github.laxika.magicalvibes.cards.b.BrambleFamiliar;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.PlantBeans;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BelunaGrandsquallSeekThrills.class, SeekThrills.class, BeanstalkWurm.class,
        PlantBeans.class, BrambleFamiliar.class, FetchQuest.class, GrizzlyBears.class, LightningBolt.class})
class BelunaGrandsquallSeekThrillsTest extends BaseCardTest {

    @Test
    void seekThrillsReturnsAllMilledAdventureCardsToHand() {
        Card adventureOne = new BeanstalkWurm();
        Card adventureTwo = new BrambleFamiliar();
        Card nonAdventureOne = new GrizzlyBears();
        Card nonAdventureTwo = new LightningBolt();
        Card nonAdventureThree = new GrizzlyBears();
        Card nonAdventureFour = new LightningBolt();
        Card nonAdventureFive = new GrizzlyBears();
        harness.setLibrary(player1, List.of(
                adventureOne, nonAdventureOne, adventureTwo, nonAdventureTwo,
                nonAdventureThree, nonAdventureFour, nonAdventureFive));

        BelunaGrandsquallSeekThrills card = new BelunaGrandsquallSeekThrills();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(adventureOne, adventureTwo);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(nonAdventureOne, nonAdventureTwo,
                        nonAdventureThree, nonAdventureFour, nonAdventureFive);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void belunaReducesPermanentAdventureSpellCost() {
        harness.addToBattlefield(player1, new BelunaGrandsquallSeekThrills());
        BeanstalkWurm wurm = new BeanstalkWurm();
        harness.setHand(player1, List.of(wurm));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Beanstalk Wurm");
    }

    @Test
    void seekThrillsDoesNothingWhenNoMilledCardHasAdventure() {
        List<Card> milled = List.of(
                new GrizzlyBears(), new LightningBolt(), new GrizzlyBears(),
                new LightningBolt(), new GrizzlyBears(), new LightningBolt(), new GrizzlyBears());
        harness.setLibrary(player1, milled);

        BelunaGrandsquallSeekThrills card = new BelunaGrandsquallSeekThrills();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrderElementsOf(milled);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }
}
