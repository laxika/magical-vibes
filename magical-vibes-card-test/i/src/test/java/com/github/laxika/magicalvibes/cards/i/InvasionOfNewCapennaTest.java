package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AlabasterHostSanctifier;
import com.github.laxika.magicalvibes.cards.b.BalduvianBarbarians;
import com.github.laxika.magicalvibes.cards.d.DeadlyDerision;
import com.github.laxika.magicalvibes.cards.g.GoldenScaleAeronaut;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyFrazzleCannon;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InvasionOfNewCapenna.class, HolyFrazzleCannon.class, LeoninScimitar.class,
        GrizzlyBears.class, BalduvianBarbarians.class, AlabasterHostSanctifier.class,
        GoldenScaleAeronaut.class, DeadlyDerision.class})
class InvasionOfNewCapennaTest extends BaseCardTest {

    @Test
    void maySacrificeArtifactThenExilesTargetArtifactOrCreatureOpponentControls() {
        Permanent sacrificedArtifact = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castInvasion();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrificedArtifact.getId());

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).containsExactlyInAnyOrder(
                opponentArtifact.getId(), opponentCreature.getId());
        harness.handlePermanentChosen(player1, opponentArtifact.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Leonin Scimitar");
        harness.assertInGraveyard(player1, "Leonin Scimitar");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void decliningSacrificeLeavesPermanentsUntouched() {
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castInvasion();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Leonin Scimitar");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void defeatedSiegeCastsBackFaceTransformed() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfNewCapenna());
        battle.setCounterCount(CounterType.DEFENSE, 0);

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent cannon = findPermanent(player1, "Holy Frazzle-Cannon");
        assertThat(cannon.isTransformed()).isTrue();
    }

    @Test
    void attackingEquippedCreatureGetsCountersOnItAndOtherMatchingCreaturesYouControl() {
        Permanent equippedCreature = addCreatureReady(player1, new BalduvianBarbarians());
        Permanent matchingCreature = addCreatureReady(player1, new BalduvianBarbarians());
        Permanent differentCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent cannon = addCreatureReady(player1, new HolyFrazzleCannon());
        Permanent opponentMatchingCreature = addCreatureReady(player2, new BalduvianBarbarians());
        cannon.setAttachedTo(equippedCreature.getId());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(equippedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(matchingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(differentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentMatchingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void equipAttachesCannonToCreatureYouControl() {
        addCreatureReady(player1, new HolyFrazzleCannon());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        Permanent cannon = gd.playerBattlefields.get(player1.getId()).get(0);
        assertThat(cannon.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void sacrificingCreatureExilesOpponentsCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new AlabasterHostSanctifier());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoldenScaleAeronaut());

        castInvasion();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Alabaster Host Sanctifier");
        harness.assertNotOnBattlefield(player2, "Golden-Scale Aeronaut");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName).containsExactly("Golden-Scale Aeronaut");
    }

    @Test
    void canSacrificeEvenWithoutAnOpposingArtifactOrCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new AlabasterHostSanctifier());

        castInvasion();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Alabaster Host Sanctifier");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void decliningWithNothingToSacrificeDoesNotExileAnything() {
        harness.addToBattlefield(player2, new AlabasterHostSanctifier());

        castInvasion();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Alabaster Host Sanctifier");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void attackTriggerStillCountersAttackerAndMatchingCreaturesAfterCannonBecomesUnattached() {
        Permanent attacker = addCreatureReady(player1, new AlabasterHostSanctifier());
        Permanent matching = addCreatureReady(player1, new AlabasterHostSanctifier());
        Permanent different = addCreatureReady(player1, new GoldenScaleAeronaut());
        Permanent cannon = harness.addToBattlefieldAndReturn(player1, new HolyFrazzleCannon());
        cannon.setAttachedTo(attacker.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        cannon.setAttachedTo(null);
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(matching.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(different.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void attackTriggerUsesLastKnownCreatureTypesWhenAttackerDiesInResponse() {
        Permanent attacker = addCreatureReady(player1, new AlabasterHostSanctifier());
        Permanent matching = addCreatureReady(player1, new AlabasterHostSanctifier());
        Permanent different = addCreatureReady(player1, new GoldenScaleAeronaut());
        Permanent cannon = harness.addToBattlefieldAndReturn(player1, new HolyFrazzleCannon());
        cannon.setAttachedTo(attacker.getId());
        harness.setHand(player2, List.of(new DeadlyDerision()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        harness.castAndResolveInstant(player2, 0, attacker.getId());
        harness.assertInGraveyard(player1, "Alabaster Host Sanctifier");
        resolveAllTriggers();

        assertThat(matching.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(different.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castInvasion() {
        harness.castFromHand(player1, new InvasionOfNewCapenna(), "{W}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
