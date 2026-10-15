package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FetchQuest;
import com.github.laxika.magicalvibes.cards.m.Mintstrosity;
import com.github.laxika.magicalvibes.cards.s.SeekThrills;
import com.github.laxika.magicalvibes.cards.f.FranticFirebolt;
import com.github.laxika.magicalvibes.cards.p.PlantBeans;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BelunaGrandsquallSeekThrills.class, SeekThrills.class, BeanstalkWurm.class,
        PlantBeans.class, BrambleFamiliar.class, FetchQuest.class, Mintstrosity.class, FranticFirebolt.class})
class BelunaGrandsquallSeekThrillsTest extends BaseCardTest {

    @Test
    void seekThrillsReturnsAllMilledAdventureCardsToHand() {
        Card adventureOne = new BeanstalkWurm();
        Card adventureTwo = new BrambleFamiliar();
        Card nonAdventureOne = new Mintstrosity();
        Card nonAdventureTwo = new FranticFirebolt();
        Card nonAdventureThree = new Mintstrosity();
        Card nonAdventureFour = new FranticFirebolt();
        Card nonAdventureFive = new Mintstrosity();
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
    void seekThrillsMillsOnlySevenAndLeavesOlderAdventureCardsInGraveyard() {
        Card oldAdventure = new BeanstalkWurm();
        Card milledAdventure = new BrambleFamiliar();
        Card eighthCard = new BeanstalkWurm();
        List<Card> nonAdventures = List.of(new Mintstrosity(), new FranticFirebolt(),
                new Mintstrosity(), new FranticFirebolt(), new Mintstrosity(), new FranticFirebolt());
        harness.setGraveyard(player1, List.of(oldAdventure));
        harness.setLibrary(player1, List.of(milledAdventure, nonAdventures.get(0), nonAdventures.get(1),
                nonAdventures.get(2), nonAdventures.get(3), nonAdventures.get(4), nonAdventures.get(5), eighthCard));
        harness.setHand(player1, List.of(new BelunaGrandsquallSeekThrills()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(milledAdventure);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(oldAdventure)
                .containsAll(nonAdventures).hasSize(7);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(eighthCard);
    }

    @Test
    void seekThrillsReturnsAdventuresWhenLibraryHasFewerThanSevenCards() {
        Card milledAdventure = new BeanstalkWurm();
        Card nonAdventure = new Mintstrosity();
        harness.setLibrary(player1, List.of(milledAdventure, nonAdventure));
        harness.setHand(player1, List.of(new BelunaGrandsquallSeekThrills()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(milledAdventure);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonAdventure);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void belunaDoesNotReduceAdventureSpellCost() {
        harness.addToBattlefield(player1, new BelunaGrandsquallSeekThrills());
        harness.setHand(player1, List.of(new BeanstalkWurm()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void belunaDoesNotReducePermanentSpellsWithoutAdventure() {
        harness.addToBattlefield(player1, new BelunaGrandsquallSeekThrills());
        harness.setHand(player1, List.of(new Mintstrosity()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Mintstrosity");
    }

    @Test
    void opponentsBelunaDoesNotReduceYourPermanentAdventureSpellCost() {
        harness.addToBattlefield(player2, new BelunaGrandsquallSeekThrills());
        harness.setHand(player1, List.of(new BeanstalkWurm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Beanstalk Wurm");
    }

    @Test
    void belunaReducesPermanentAdventureSpellCastFromExile() {
        harness.addToBattlefield(player1, new BelunaGrandsquallSeekThrills());
        Card wurm = new BeanstalkWurm();
        harness.setHand(player1, List.of(wurm));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        assertThat(gd.findExiledCard(wurm.getId())).isNotNull();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromExile(player1, wurm.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Beanstalk Wurm");
        assertThat(gd.findExiledCard(wurm.getId())).isNull();
    }

    @Test
    void seekThrillsDoesNothingWhenNoMilledCardHasAdventure() {
        List<Card> milled = List.of(
                new Mintstrosity(), new FranticFirebolt(), new Mintstrosity(),
                new FranticFirebolt(), new Mintstrosity(), new FranticFirebolt(), new Mintstrosity());
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
