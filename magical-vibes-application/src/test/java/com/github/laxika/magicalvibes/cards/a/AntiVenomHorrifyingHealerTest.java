package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.z.Zombify;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({AntiVenomHorrifyingHealer.class, GrizzlyBears.class, Shock.class, Zombify.class})
class AntiVenomHorrifyingHealerTest extends BaseCardTest {

    @Test
    @DisplayName("When cast, returns a target creature card from the graveyard")
    void returnsCreatureWhenCast() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new AntiVenomHorrifyingHealer()));
        addWhiteMana(5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Anti-Venom, Horrifying Healer");
    }

    @Test
    @DisplayName("Does not return a creature when put onto the battlefield without being cast")
    void doesNotReturnCreatureWhenNotCast() {
        Card antiVenom = new AntiVenomHorrifyingHealer();
        Card creature = new GrizzlyBears();
        Zombify zombify = new Zombify();
        harness.setGraveyard(player1, List.of(antiVenom, creature));
        harness.setHand(player1, List.of(zombify));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, antiVenom.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Anti-Venom, Horrifying Healer");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(creature.getId(), zombify.getId());
    }

    @Test
    @DisplayName("Prevents spell damage and puts that many +1/+1 counters on itself")
    void preventsSpellDamageAndAddsCounters() {
        AntiVenomHorrifyingHealer antiVenomCard = new AntiVenomHorrifyingHealer();
        antiVenomCard.setToughness(1);
        harness.addToBattlefield(player2, antiVenomCard);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Anti-Venom, Horrifying Healer"));
        harness.passBothPriorities();

        Permanent antiVenom = findPermanent(player2, "Anti-Venom, Horrifying Healer");
        assertThat(antiVenom.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Prevents combat damage and puts that many +1/+1 counters on itself")
    void preventsCombatDamageAndAddsCounters() {
        AntiVenomHorrifyingHealer antiVenomCard = new AntiVenomHorrifyingHealer();
        antiVenomCard.setToughness(1);
        Permanent blocker = new Permanent(antiVenomCard);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Anti-Venom, Horrifying Healer")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void addWhiteMana(int amount) {
        harness.addMana(player1, ManaColor.WHITE, amount);
    }
}
