package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CullingDais;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SandbenderScavengers.class, CullingDais.class, GrizzlyBears.class, HillGiant.class, Island.class, Ornithopter.class})
class SandbenderScavengersTest extends BaseCardTest {

    @Test
    void sacrificePutsCounterAndDeathCanReturnCreatureUpToLastKnownPower() {
        Card scavengers = new SandbenderScavengers();
        Permanent scavengersPermanent = addCreatureReady(player1, scavengers);
        harness.addToBattlefield(player1, new CullingDais());
        Permanent sacrificedPermanent = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, sacrificedPermanent.getId());
        resolveAllTriggers();

        assertThat(scavengersPermanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        Card validTarget = new GrizzlyBears();
        Card tooExpensiveTarget = new HillGiant();
        Card nonCreatureTarget = new Island();
        Card opponentTarget = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(opponentTarget));
        harness.setGraveyard(player1, List.of(validTarget, tooExpensiveTarget, nonCreatureTarget));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, scavengersPermanent));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(validTarget.getId());
        assertThat(choice.validCardIds()).doesNotContain(tooExpensiveTarget.getId(), nonCreatureTarget.getId(), opponentTarget.getId());

        harness.handleMultipleCardsChosen(player1, List.of(validTarget.getId()));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(scavengers.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(validTarget.getId()));
    }

    @Test
    void decliningExileLeavesSourceInGraveyard() {
        Card scavengers = new SandbenderScavengers();
        Card target = new GrizzlyBears();
        Permanent scavengersPermanent = addCreatureReady(player1, scavengers);
        harness.setGraveyard(player1, List.of(target));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, scavengersPermanent));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(scavengers.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(scavengers.getId(), target.getId());
    }

    @Test
    void sacrificingNonCreaturePermanentPutsCounterOnScavengers() {
        Permanent scavengers = addCreatureReady(player1, new SandbenderScavengers());
        harness.addToBattlefield(player1, new CullingDais());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 1, null, null);
        resolveAllTriggers();

        assertThat(scavengers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Culling Dais");
    }

    @Test
    void opponentsSacrificeDoesNotPutCounterOnScavengers() {
        Permanent scavengers = addCreatureReady(player1, new SandbenderScavengers());
        harness.addToBattlefield(player2, new CullingDais());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player2, 0, null, null);
        harness.handlePermanentChosen(player2, victim.getId());
        resolveAllTriggers();

        assertThat(scavengers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void canExileScavengersEvenWithoutLegalReturnTarget() {
        Card scavengers = new SandbenderScavengers();
        Permanent permanent = addCreatureReady(player1, scavengers);
        Card tooExpensive = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(tooExpensive));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, permanent));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId)
                .contains(scavengers.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void negativeLastKnownPowerDoesNotAllowZeroManaCreatureTarget() {
        Card scavengers = new SandbenderScavengers();
        Permanent permanent = addCreatureReady(player1, scavengers);
        Card target = new Ornithopter();
        harness.setGraveyard(player1, List.of(target));
        permanent.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        harness.runStateBasedActions();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId)
                .contains(scavengers.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player1, "Ornithopter");
    }

    @Test
    void sacrificingScavengersDoesNotTriggerItsCounterAbility() {
        Card scavengers = new SandbenderScavengers();
        Permanent permanent = addCreatureReady(player1, scavengers);
        harness.addToBattlefield(player1, new CullingDais());

        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, permanent.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Sandbender Scavengers");
        assertThat(findPermanent(player1, "Culling Dais").getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
