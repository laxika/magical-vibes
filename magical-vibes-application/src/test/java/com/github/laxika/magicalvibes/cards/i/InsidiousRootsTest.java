package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FungalPlots;
import com.github.laxika.magicalvibes.cards.c.CeaseDesist;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GravestoneStrider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InsidiousRoots.class, FungalPlots.class, GrizzlyBears.class, GravestoneStrider.class, CeaseDesist.class})
class InsidiousRootsTest extends BaseCardTest {

    @Test
    @DisplayName("A creature card leaving the graveyard creates a Plant and puts a counter on each Plant")
    void creatureCardLeavingGraveyardCreatesAndPumpsPlant() {
        harness.addToBattlefield(player1, new InsidiousRoots());
        harness.addToBattlefield(player1, new FungalPlots());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent plant = findPermanent(player1, "Plant");
        assertThat(plant.getCard().getSubtypes()).contains(CardSubtype.PLANT);
        assertThat(plant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, plant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, plant)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creature tokens can tap for one mana of any color")
    void creatureTokensCanTapForAnyColor() {
        harness.addToBattlefield(player1, new InsidiousRoots());
        harness.addToBattlefield(player1, new FungalPlots());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent plant = findPermanent(player1, "Plant");
        plant.setSummoningSick(false);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(plant), null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void separateCostAndResolutionDeparturesEachCreateAPlant() {
        harness.addToBattlefield(player1, new InsidiousRoots());
        Card source = new GravestoneStrider();
        Card target = new GravestoneStrider();
        harness.setGraveyard(player1, List.of(source, target));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();
        Permanent firstPlant = findPermanent(player1, "Plant");
        assertThat(firstPlant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> plants = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Plant")).toList();
        assertThat(plants).hasSize(2);
        assertThat(firstPlant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(plants).extracting(p -> p.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .containsExactlyInAnyOrder(2, 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void opponentsCreatureDepartureDoesNotTriggerYourRoots() {
        harness.addToBattlefield(player1, new InsidiousRoots());
        harness.addToBattlefield(player2, new InsidiousRoots());
        Card source = new GravestoneStrider();
        Card target = new InsidiousRoots();
        harness.setGraveyard(player2, List.of(source, target));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.ensurePriority(player2);

        harness.activateGraveyardAbilityWithGraveyardTargets(player2, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        assertThat(findPermanent(player2, "Plant").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void grantedManaAbilityAlsoWorksOnNonPlantCreatureTokens() {
        harness.addToBattlefield(player1, new InsidiousRoots());
        harness.addToBattlefield(player1, new FungalPlots());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent saproling = findPermanent(player1, "Saproling");
        assertThat(saproling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        saproling.setSummoningSick(false);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(saproling), null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(saproling.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void newPlantCannotTapForManaWhileSummoningSick() {
        harness.addToBattlefield(player1, new InsidiousRoots());
        Card source = new GravestoneStrider();
        Card target = new InsidiousRoots();
        harness.setGraveyard(player1, List.of(source, target));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent plant = findPermanent(player1, "Plant");
        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(plant), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(plant.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void simultaneousCreatureDeparturesCreateOnlyOnePlant() {
        harness.addToBattlefield(player1, new InsidiousRoots());
        Card first = new GravestoneStrider();
        Card second = new GravestoneStrider();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setLibrary(player2, List.of(new InsidiousRoots()));
        harness.setHand(player1, List.of(new CeaseDesist()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, 0, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(findPermanent(player1, "Plant").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(first, second);
    }

    @Test
    void nonCreatureDepartureDoesNotCreateAPlant() {
        harness.addToBattlefield(player1, new InsidiousRoots());
        Card target = new InsidiousRoots();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player2, List.of(new InsidiousRoots()));
        harness.setHand(player1, List.of(new CeaseDesist()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, 0, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
        assertThat(gd.stack).isEmpty();
    }
}
