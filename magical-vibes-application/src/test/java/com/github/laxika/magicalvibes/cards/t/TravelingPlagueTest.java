package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AvenFlock;
import com.github.laxika.magicalvibes.cards.e.Execute;
import com.github.laxika.magicalvibes.cards.m.MossfireEgg;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TravelingPlague.class, AvenFlock.class, Execute.class, MossfireEgg.class})
class TravelingPlagueTest extends BaseCardTest {

    @Test
    @DisplayName("Adds a plague counter on every upkeep and gives the enchanted creature -1/-1 per counter")
    void addsCountersOnEveryUpkeepAndShrinksEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new AvenFlock());
        castTravelingPlagueOn(player1, creature);
        Permanent plague = findPermanent(player1, "Traveling Plague");

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(plague.getCounterCount(CounterType.PLAGUE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(plague.getCounterCount(CounterType.PLAGUE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Returns without counters and lets the former creature controller choose the new creature")
    void returnsWithoutCountersAndFormerControllerChoosesCreature() {
        Permanent oldCreature = addCreatureReady(player2, new AvenFlock());
        Permanent playerOneCreature = addCreatureReady(player1, new AvenFlock());
        Permanent playerTwoCreature = addCreatureReady(player2, new AvenFlock());
        castTravelingPlagueOn(player1, oldCreature);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        Permanent plague = findPermanent(player1, "Traveling Plague");
        assertThat(plague.getCounterCount(CounterType.PLAGUE)).isEqualTo(1);

        destroyCreature(player1, oldCreature);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validPermanentIds()).contains(playerOneCreature.getId(), playerTwoCreature.getId());

        harness.handlePermanentChosen(player2, playerOneCreature.getId());

        Permanent returnedPlague = findPermanent(player1, "Traveling Plague");
        assertThat(returnedPlague.getAttachedTo()).isEqualTo(playerOneCreature.getId());
        assertThat(returnedPlague.getCounterCount(CounterType.PLAGUE)).isZero();
        harness.assertNotInGraveyard(player1, "Traveling Plague");
    }

    @Test
    @DisplayName("Stays in its owner's graveyard when no legal creature remains")
    void staysInGraveyardWithoutLegalCreature() {
        Permanent oldCreature = addCreatureReady(player2, new AvenFlock());
        castTravelingPlagueOn(player1, oldCreature);

        destroyCreature(player1, oldCreature);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Traveling Plague");
        harness.assertNotOnBattlefield(player1, "Traveling Plague");
        harness.assertNotOnBattlefield(player2, "Traveling Plague");
    }

    @Test
    @DisplayName("Returns when the enchanted creature leaves for its owner's hand")
    void returnsWhenEnchantedCreatureReturnsToHand() {
        Permanent oldCreature = addCreatureReady(player2, new AvenFlock());
        Permanent replacementCreature = addCreatureReady(player1, new AvenFlock());
        castTravelingPlagueOn(player1, oldCreature);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, oldCreature));
        resolveAllTriggers();

        Permanent returnedPlague = findPermanent(player1, "Traveling Plague");
        assertThat(returnedPlague.getAttachedTo()).isEqualTo(replacementCreature.getId());
        assertThat(returnedPlague.getCounterCount(CounterType.PLAGUE)).isZero();
        harness.assertInHand(player2, "Aven Flock");
        harness.assertNotInGraveyard(player1, "Traveling Plague");
    }

    @Test
    @DisplayName("Can enchant only a creature")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MossfireEgg());
        harness.setHand(player1, List.of(new TravelingPlague()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castTravelingPlagueOn(Player caster, Permanent target) {
        harness.setHand(caster, List.of(new TravelingPlague()));
        harness.addMana(caster, ManaColor.BLACK, 5);
        harness.castEnchantment(caster, 0, target.getId());
        harness.passBothPriorities();
    }

    private void destroyCreature(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Execute()));
        harness.addMana(caster, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(caster, 0, target.getId());
    }
}
