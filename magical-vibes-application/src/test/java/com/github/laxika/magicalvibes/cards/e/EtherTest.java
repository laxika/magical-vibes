package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.StreamOfLife;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Ether.class, LightningBolt.class, GrizzlyBears.class, StreamOfLife.class, Plains.class})
class EtherTest extends BaseCardTest {

    @Test
    void exilesItselfAddsBlueManaAndRegistersCopy() {
        harness.addToBattlefield(player1, new Ether());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Ether");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void copiesTheNextInstantAndConsumesTheRegistration() {
        harness.addToBattlefield(player1, new Ether());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getDescription().startsWith("Copy Lightning Bolt"));
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount).doesNotContainKey(player1.getId());
    }

    @Test
    void doesNotConsumeTheRegistrationForACreatureSpell() {
        harness.addToBattlefield(player1, new Ether());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getDescription() != null && entry.getDescription().startsWith("Copy "));
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void copyResolvesWithOriginalTargetsAndOnlyTheFirstSpellIsCopied() {
        harness.addToBattlefield(player1, new Ether());
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.stack).isEmpty();
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 11);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copyCanChooseANewTargetWithoutChangingTheOriginal() {
        harness.addToBattlefield(player1, new Ether());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
    }

    @Test
    void copiesSorceryAndPreservesXWithoutRequiringEthersManaToBeSpent() {
        harness.addToBattlefield(player1, new Ether());
        harness.activateAbility(player1, 0, 0, null, null);
        gd.playerManaPools.get(player1.getId()).clear();
        harness.setHand(player1, List.of(new StreamOfLife()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 3, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 26);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsSpellDoesNotConsumeTheDelayedCopy() {
        harness.addToBattlefield(player1, new Ether());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 17);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 14);
    }

    @Test
    void unusedCopyExpiresAtTheEndOfTheTurn() {
        harness.addToBattlefield(player1, new Ether());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.setLibrary(player1, List.of(new Plains(), new Plains()));
        harness.setLibrary(player2, List.of(new Plains(), new Plains()));

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 17);
        assertThat(gd.stack).isEmpty();
    }
}
