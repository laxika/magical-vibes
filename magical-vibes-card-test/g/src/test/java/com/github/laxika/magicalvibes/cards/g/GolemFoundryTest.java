package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.a.AlphaTyrranax;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GolemFoundry.class, Memnite.class, AlphaTyrranax.class})
class GolemFoundryTest extends BaseCardTest {

    @Test
    @DisplayName("Artifact cast trigger offers its optional counter only at resolution")
    void castingArtifactQueuesTriggerBeforeOfferingChoice() {
        harness.addToBattlefield(player1, new GolemFoundry());
        harness.setHand(player1, List.of(new Memnite()));

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Accepting the may-ability puts a charge counter on Golem Foundry")
    void acceptingAddsChargeCounter() {
        harness.addToBattlefield(player1, new GolemFoundry());
        harness.setHand(player1, List.of(new Memnite()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve charge counter triggered ability
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // resolve Memnite

        Permanent foundry = findPermanent(player1, "Golem Foundry");
        assertThat(foundry.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the may-ability does not add a charge counter")
    void decliningDoesNotAddChargeCounter() {
        harness.addToBattlefield(player1, new GolemFoundry());
        harness.setHand(player1, List.of(new Memnite()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve charge counter triggered ability
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities(); // resolve Memnite

        Permanent foundry = findPermanent(player1, "Golem Foundry");
        assertThat(foundry.getCounterCount(CounterType.CHARGE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Casting a non-artifact spell does not trigger")
    void nonArtifactSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new GolemFoundry());
        harness.setHand(player1, List.of(new AlphaTyrranax()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        // No may-ability prompt for charge counter
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();

        Permanent foundry = findPermanent(player1, "Golem Foundry");
        assertThat(foundry.getCounterCount(CounterType.CHARGE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Opponent casting an artifact spell does not trigger")
    void opponentArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new GolemFoundry());
        harness.setHand(player2, List.of(new Memnite()));
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve Memnite

        Permanent foundry = findPermanent(player1, "Golem Foundry");
        assertThat(foundry.getCounterCount(CounterType.CHARGE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Activating with 3 charge counters creates a 3/3 Golem token")
    void activateCreatesGolemToken() {
        Permanent foundry = harness.addToBattlefieldAndReturn(player1, new GolemFoundry());
        foundry.setCounterCount(CounterType.CHARGE, 3);

        int foundryIndex = gd.playerBattlefields.get(player1.getId()).indexOf(foundry);
        harness.activateAbility(player1, foundryIndex, null, null);
        harness.passBothPriorities(); // resolve activated ability

        assertThat(foundry.getCounterCount(CounterType.CHARGE)).isEqualTo(0);

        // 3/3 Golem token is on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Golem")
                        && p.getCard().getPower() == 3
                        && p.getCard().getToughness() == 3
                        && p.getCard().hasType(CardType.ARTIFACT)
                        && p.getCard().hasType(CardType.CREATURE)
                        && p.getCard().getSubtypes().contains(CardSubtype.GOLEM)
                        && p.getCard().getColors().isEmpty());
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate with fewer than 3 charge counters")
    void cannotActivateWithFewerThanThreeCounters() {
        Permanent foundry = harness.addToBattlefieldAndReturn(player1, new GolemFoundry());
        foundry.setCounterCount(CounterType.CHARGE, 2);

        int foundryIndex = gd.playerBattlefields.get(player1.getId()).indexOf(foundry);
        assertThatThrownBy(() -> harness.activateAbility(player1, foundryIndex, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activating with more than 3 charge counters only removes 3")
    void activateRemovesExactlyThreeCounters() {
        Permanent foundry = harness.addToBattlefieldAndReturn(player1, new GolemFoundry());
        foundry.setCounterCount(CounterType.CHARGE, 5);

        int foundryIndex = gd.playerBattlefields.get(player1.getId()).indexOf(foundry);
        harness.activateAbility(player1, foundryIndex, null, null);
        harness.passBothPriorities();

        assertThat(foundry.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counters are paid immediately and a tapped Foundry can activate twice")
    void tappedFoundryCanActivateTwiceBeforeEitherAbilityResolves() {
        Permanent foundry = harness.addToBattlefieldAndReturn(player1, new GolemFoundry());
        foundry.setCounterCount(CounterType.CHARGE, 6);
        foundry.tap();

        harness.activateAbility(player1, 0, null, null);
        assertThat(foundry.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.activateAbility(player1, 0, null, null);
        assertThat(foundry.getCounterCount(CounterType.CHARGE)).isZero();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Golem")).hasSize(2);
        assertThat(foundry.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Casting Golem Foundry does not trigger its own ability")
    void castingFoundryDoesNotTriggerItself() {
        harness.setHand(player1, List.of(new GolemFoundry()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Golem Foundry").getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Casting a second Foundry triggers the existing Foundry only")
    void castingNoncreatureArtifactTriggersExistingFoundry() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new GolemFoundry());
        harness.setHand(player1, List.of(new GolemFoundry()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(original.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).get(1).getCounterCount(CounterType.CHARGE)).isZero();
    }
}
