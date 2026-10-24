package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KalitasTraitorOfGhet;
import com.github.laxika.magicalvibes.cards.w.WheelOfSunAndMoon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FailOnLegacyDepartureFallback;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DemonicHordes.class, Island.class, GrizzlyBears.class})
class DemonicHordesTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target land")
    void destroysTargetLand() {
        Permanent hordes = addCreatureReady(player1, new DemonicHordes());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(hordes.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(land.getId()));
    }

    @Test
    @DisplayName("Can destroy a land it controls")
    void destroysTargetLandItControls() {
        Permanent hordes = addCreatureReady(player1, new DemonicHordes());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(hordes.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(land.getId()));
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        addCreatureReady(player1, new DemonicHordes());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Paying the upkeep cost keeps the creature untapped")
    void payingUpkeepCostKeepsCreatureUntapped() {
        Permanent hordes = addCreatureReady(player1, new DemonicHordes());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(hordes.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(land.getId()));
    }

    @Test
    @DisplayName("If unpaid without a land, only taps the creature")
    void unpaidUpkeepWithoutLandOnlyTapsCreature() {
        Permanent hordes = addCreatureReady(player1, new DemonicHordes());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(hordes.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(hordes.getId()));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("If unpaid, taps and sacrifices the only land")
    void unpaidUpkeepSacrificesOnlyLand() {
        Permanent hordes = addCreatureReady(player1, new DemonicHordes());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(hordes.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(land.getId()));
    }

    @Test
    @DisplayName("If unpaid, an opponent chooses which land to sacrifice")
    void opponentChoosesLandToSacrifice() {
        Permanent hordes = addCreatureReady(player1, new DemonicHordes());
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new Island());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstLand.getId(), secondLand.getId());

        harness.handlePermanentChosen(player2, firstLand.getId());

        assertThat(hordes.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(firstLand.getId()))
                .anyMatch(permanent -> permanent.getId().equals(secondLand.getId()));
    }

    @Test
    @DisplayName("Unpaid upkeep still sacrifices a land when Demonic Hordes is already tapped")
    void unpaidUpkeepWhileTappedStillSacrificesLand() {
        Permanent hordes = addCreatureReady(player1, new DemonicHordes());
        harness.addToBattlefield(player1, new Island());

        advanceToUpkeep(player1);
        hordes.tap();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(hordes.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertInGraveyard(player1, "Island");
    }

    @Test
    @DisplayName("Can destroy a land in response to its own upkeep trigger")
    void activatesInResponseToUpkeepTrigger() {
        Permanent hordes = addCreatureReady(player1, new DemonicHordes());
        harness.addToBattlefield(player1, new Island());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Island());

        advanceToUpkeep(player1);
        harness.activateAbility(player1, 0, null, opposingLand.getId());
        harness.passBothPriorities();

        assertThat(hordes.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Island");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Island");
        harness.assertOnBattlefield(player1, "Demonic Hordes");
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent hordes = addCreatureReady(player1, new DemonicHordes());
        harness.addToBattlefield(player1, new Island());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(hordes.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Island");
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, WheelOfSunAndMoon.class, DryadArbor.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("With two opponents, the lone land's death replacement is asked before it is sacrificed")
    void chosenOpponentsLoneLandAsksTheDeathReplacementBeforeItIsSacrificed() {
        Player player3 = addThirdPlayer();
        harness.addToBattlefield(player2, new KalitasTraitorOfGhet());
        Permanent hordes = addCreatureReady(player1, new DemonicHordes());
        Permanent wheel = harness.addToBattlefieldAndReturn(player1, new WheelOfSunAndMoon());
        wheel.setAttachedTo(player1.getId());
        Permanent arbor = harness.addToBattlefieldAndReturn(player1, new DryadArbor());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        PendingInteraction.PermanentChoice opponentChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(opponentChoice.playerId()).isEqualTo(player1.getId());
        assertThat(opponentChoice.validIds()).containsExactlyInAnyOrder(player2.getId(), player3.getId());
        harness.handlePermanentChosen(player1, player3.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("Put it on the bottom of its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(arbor);

        harness.handleListChoice(player1, "Put it on the bottom of its owner's library");

        assertThat(gd.playerDecks.get(player1.getId())).extracting(card -> card.getName()).contains("Dryad Arbor");
        harness.assertNotOnBattlefield(player1, "Dryad Arbor");
        harness.assertNotOnBattlefield(player2, "Zombie");
        assertThat(hordes.isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Player addThirdPlayer() {
        UUID player3Id = UUID.randomUUID();
        Player player3 = new Player(player3Id, "Charlie");
        gd.playerIds.add(player3Id);
        gd.orderedPlayerIds.add(player3Id);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(player3Id, "Charlie");
        gd.playerDecks.put(player3Id, new ArrayList<>());
        gd.playerHands.put(player3Id, new ArrayList<>());
        gd.playerBattlefields.put(player3Id, new ArrayList<>());
        gd.playerGraveyards.put(player3Id, new ArrayList<>());
        gd.playerCommandZones.put(player3Id, new ArrayList<>());
        gd.playerManaPools.put(player3Id, new ManaPool());
        gd.playerLifeTotals.put(player3Id, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), player3Id, "Charlie");
        return player3;
    }
}
