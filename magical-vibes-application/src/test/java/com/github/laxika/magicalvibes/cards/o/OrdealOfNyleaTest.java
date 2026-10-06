package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RayOfDissolution;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrdealOfNylea.class, BronzeSable.class, Forest.class, Plains.class, RayOfDissolution.class})
class OrdealOfNyleaTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking enchanted creature gets a +1/+1 counter")
    void attackPutsCounterOnEnchantedCreature() {
        Permanent creature = castOnBronzeSable();

        attack(creature);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Ordeal of Nylea");
    }

    @Test
    @DisplayName("Third +1/+1 counter sacrifices the Aura and searches for two tapped basic lands")
    void thirdCounterSacrificesAuraAndSearchesForTwoBasicLands() {
        Permanent creature = castOnBronzeSable();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Forest forest = new Forest();
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(forest, plains, new BronzeSable()));

        attack(creature);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertNotOnBattlefield(player1, "Ordeal of Nylea");
        harness.assertInGraveyard(player1, "Ordeal of Nylea");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(2)
                .allMatch(Permanent::isTapped);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Destroying the Aura without sacrificing it does not search")
    void destructionDoesNotSearch() {
        castOnBronzeSable();
        Permanent aura = findPermanent(player1, "Ordeal of Nylea");
        harness.setLibrary(player1, List.of(new Forest(), new Plains()));

        harness.setHand(player2, List.of(new RayOfDissolution()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, aura.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Ordeal of Nylea");
    }

    @Test
    @DisplayName("Three existing counters do not sacrifice the Aura until its attack trigger resolves")
    void existingCountersWaitForAttack() {
        Permanent creature = addCreatureReady(player1, new BronzeSable());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new OrdealOfNylea()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of(new Forest()));

        harness.assertOnBattlefield(player1, "Ordeal of Nylea");
        assertThat(gd.stack).isEmpty();

        attack(creature);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        harness.assertInGraveyard(player1, "Ordeal of Nylea");
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The search may find zero basic lands even when two are available")
    void mayFindZeroLands() {
        Permanent creature = castOnBronzeSable();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLibrary(player1, List.of(new Forest(), new Plains()));

        attack(creature);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Plains");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The search may stop after finding one land")
    void mayFindOnlyOneLand() {
        Permanent creature = castOnBronzeSable();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLibrary(player1, List.of(new Forest(), new Plains()));

        attack(creature);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Plains");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Enchanting an opponent's attacker searches the Aura controller's library")
    void opponentAttackerBenefitsAuraController() {
        Permanent creature = addCreatureReady(player2, new BronzeSable());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new OrdealOfNylea()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Plains()));

        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(creature)));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Ordeal of Nylea");
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Plains");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @CardUsed({RestInPeace.class})
    @DisplayName("Sacrificing the Aura still searches when Rest in Peace exiles it")
    void sacrificeSearchesDespiteGraveyardReplacement() {
        Permanent creature = castOnBronzeSable();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addToBattlefield(player1, new RestInPeace());
        harness.setLibrary(player1, List.of(new Forest()));

        attack(creature);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertNotOnBattlefield(player1, "Ordeal of Nylea");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName()).contains("Ordeal of Nylea");
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent castOnBronzeSable() {
        Permanent creature = addCreatureReady(player1, new BronzeSable());

        harness.setHand(player1, List.of(new OrdealOfNylea()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        return creature;
    }

    private void attack(Permanent creature) {
        int creatureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(creature);
        declareAttackers(List.of(creatureIndex));
        harness.passBothPriorities();
    }
}
