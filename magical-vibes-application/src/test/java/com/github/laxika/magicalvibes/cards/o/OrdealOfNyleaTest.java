package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.q.QuietPurity;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrdealOfNyleaTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking enchanted creature gets a +1/+1 counter")
    void attackPutsCounterOnEnchantedCreature() {
        Permanent creature = castOnGrizzlyBears();

        attack(creature);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Ordeal of Nylea");
    }

    @Test
    @DisplayName("Third +1/+1 counter sacrifices the Aura and searches for two tapped basic lands")
    void thirdCounterSacrificesAuraAndSearchesForTwoBasicLands() {
        Permanent creature = castOnGrizzlyBears();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Forest forest = new Forest();
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(forest, plains, new GrizzlyBears()));

        attack(creature);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

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
        castOnGrizzlyBears();
        Permanent aura = findPermanent(player1, "Ordeal of Nylea");
        harness.setLibrary(player1, List.of(new Forest(), new Plains()));

        harness.setHand(player2, List.of(new QuietPurity()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, aura.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Ordeal of Nylea");
    }

    private Permanent castOnGrizzlyBears() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new OrdealOfNylea()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        return creature;
    }

    private void attack(Permanent creature) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int creatureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(creature);
        gs.declareAttackers(gd, player1, List.of(creatureIndex));
        harness.passBothPriorities();
    }
}
