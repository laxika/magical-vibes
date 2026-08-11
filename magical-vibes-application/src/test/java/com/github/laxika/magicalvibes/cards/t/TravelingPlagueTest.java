package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TravelingPlagueTest extends BaseCardTest {

    @Test
    @DisplayName("Adds a plague counter on every upkeep and gives the enchanted creature -1/-1 per counter")
    void addsCountersOnEveryUpkeepAndShrinksEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new ColossalDreadmaw());
        castTravelingPlagueOn(player1, creature);
        Permanent plague = findPermanent(player1, "Traveling Plague");

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(plague.getCounterCount(CounterType.PLAGUE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(plague.getCounterCount(CounterType.PLAGUE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Returns without counters and lets the former creature controller choose the new creature")
    void returnsWithoutCountersAndFormerControllerChoosesCreature() {
        Permanent oldCreature = addCreatureReady(player2, new ColossalDreadmaw());
        Permanent playerOneCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent playerTwoCreature = addCreatureReady(player2, new GrizzlyBears());
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
        Permanent oldCreature = addCreatureReady(player2, new ColossalDreadmaw());
        castTravelingPlagueOn(player1, oldCreature);

        destroyCreature(player1, oldCreature);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Traveling Plague");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Traveling Plague"));
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
        harness.setHand(caster, List.of(new DoomBlade()));
        harness.addMana(caster, ManaColor.BLACK, 2);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
    }
}
