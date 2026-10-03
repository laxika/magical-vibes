package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Skullcrack;
import com.github.laxika.magicalvibes.cards.s.SpiderManNoMore;
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

@CardUsed({AntiVenomHorrifyingHealer.class, GrizzlyBears.class, Shock.class, Zombify.class,
        Skullcrack.class, SpiderManNoMore.class})
class AntiVenomHorrifyingHealerTest extends BaseCardTest {

    @Test
    @DisplayName("When cast, returns a target creature card from the graveyard")
    void returnsCreatureWhenCast() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.castFromHand(player1, new AntiVenomHorrifyingHealer(), "{W}{W}{W}{W}{W}");
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

        harness.castAndResolveSorcery(player1, 0, antiVenom.getId());

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
        harness.addToBattlefield(player2, antiVenomCard);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Anti-Venom, Horrifying Healer"));

        Permanent antiVenom = findPermanent(player2, "Anti-Venom, Horrifying Healer");
        assertThat(antiVenom.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(antiVenom.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Prevents combat damage and puts that many +1/+1 counters on itself")
    void preventsCombatDamageAndAddsCounters() {
        AntiVenomHorrifyingHealer antiVenomCard = new AntiVenomHorrifyingHealer();
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, antiVenomCard);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Anti-Venom, Horrifying Healer")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    void stillAddsCountersWhenDamageCannotBePrevented() {
        Permanent antiVenom = harness.addToBattlefieldAndReturn(player2, new AntiVenomHorrifyingHealer());
        harness.setHand(player1, List.of(new Skullcrack(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, antiVenom.getId());

        assertThat(antiVenom.getMarkedDamage()).isEqualTo(2);
        assertThat(antiVenom.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Anti-Venom, Horrifying Healer");
    }

    @Test
    void doesNotPreventDamageAfterLosingAbilities() {
        Permanent antiVenom = harness.addToBattlefieldAndReturn(player2, new AntiVenomHorrifyingHealer());
        harness.setHand(player1, List.of(new SpiderManNoMore(), new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, antiVenom.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, antiVenom.getId());

        harness.assertNotOnBattlefield(player2, "Anti-Venom, Horrifying Healer");
        harness.assertInGraveyard(player2, "Anti-Venom, Horrifying Healer");
    }

    @Test
    void doesNotProtectOtherCreatures() {
        Permanent antiVenom = harness.addToBattlefieldAndReturn(player2, new AntiVenomHorrifyingHealer());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(antiVenom.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void onlyTargetsCreatureCardsInControllersGraveyard() {
        Card creature = new GrizzlyBears();
        Card noncreature = new Shock();
        Card opposingCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature, noncreature));
        harness.setGraveyard(player2, List.of(opposingCreature));
        harness.castFromHand(player1, new AntiVenomHorrifyingHealer(), "{W}{W}{W}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
