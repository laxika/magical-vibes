package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.ArtisanOfKozilek;
import com.github.laxika.magicalvibes.cards.b.BlastedLandscape;
import com.github.laxika.magicalvibes.cards.j.JungleBasin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VolcanicOffering.class, BlastedLandscape.class, GrizzlyBears.class, ArtisanOfKozilek.class, JungleBasin.class})
class VolcanicOfferingTest extends BaseCardTest {

    @Test
    void controllerAndOpponentChooseTheTargets() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new BlastedLandscape());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VolcanicOffering()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0, List.of(land.getId(), creature.getId()));

        PendingInteraction.PermanentChoice opponentForLand =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(opponentForLand.playerId()).isEqualTo(player1.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player2, land.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player2, creature.getId());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card instanceof BlastedLandscape)
                .anyMatch(card -> card instanceof GrizzlyBears);
    }

    @Test
    void sharedCreatureTargetTakesFourteenDamage() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new JungleBasin());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArtisanOfKozilek());
        castOffering(land, creature);
        chooseOpponentTargets(land, creature);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Artisan of Kozilek");
        harness.assertInGraveyard(player2, "Jungle Basin");
    }

    @Test
    void distinctTargetsEachReceiveTheirOwnEffect() {
        Permanent firstLand = harness.addToBattlefieldAndReturn(player2, new JungleBasin());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player2, new JungleBasin());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new ArtisanOfKozilek());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new ArtisanOfKozilek());
        castOffering(firstLand, firstCreature);
        chooseOpponentTargets(secondLand, secondCreature);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(firstCreature, secondCreature);
        assertThat(firstCreature.getMarkedDamage()).isEqualTo(7);
        assertThat(secondCreature.getMarkedDamage()).isEqualTo(7);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card instanceof JungleBasin).hasSize(2);
    }

    @Test
    void missingLandTargetsDoNotPreventCreatureDamage() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new JungleBasin());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArtisanOfKozilek());
        castOffering(land, creature);
        chooseOpponentTargets(land, creature);
        gd.playerBattlefields.get(player2.getId()).remove(land);
        harness.setGraveyard(player2, List.of(land.getCard()));

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Artisan of Kozilek");
        harness.assertInGraveyard(player1, "Volcanic Offering");
    }

    @Test
    void chosenOpponentCanChooseAnotherOpponentsPermanents() {
        Player player3 = addThirdPlayer();
        Permanent land = harness.addToBattlefieldAndReturn(player3, new JungleBasin());
        Permanent creature = harness.addToBattlefieldAndReturn(player3, new ArtisanOfKozilek());
        harness.addToBattlefield(player2, new JungleBasin());
        harness.addToBattlefield(player2, new ArtisanOfKozilek());
        castOffering(land, creature);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player2, land.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player2, creature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentWithoutPermanentsCanStillChooseTargets() {
        Player player3 = addThirdPlayer();
        Permanent land = harness.addToBattlefieldAndReturn(player3, new JungleBasin());
        Permanent creature = harness.addToBattlefieldAndReturn(player3, new ArtisanOfKozilek());
        castOffering(land, creature);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(player2.getId(), player3.getId());
    }

    private void castOffering(Permanent land, Permanent creature) {
        harness.setHand(player1, List.of(new VolcanicOffering()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstant(player1, 0, List.of(land.getId(), creature.getId()));
    }

    private void chooseOpponentTargets(Permanent land, Permanent creature) {
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player2, land.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player2, creature.getId());
    }

    private Player addThirdPlayer() {
        UUID id = UUID.randomUUID();
        Player player = new Player(id, "Charlie");
        gd.playerIds.add(id);
        gd.orderedPlayerIds.add(id);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(id, "Charlie");
        gd.playerDecks.put(id, new ArrayList<>());
        gd.playerHands.put(id, new ArrayList<>());
        gd.playerBattlefields.put(id, new ArrayList<>());
        gd.playerGraveyards.put(id, new ArrayList<>());
        gd.playerCommandZones.put(id, new ArrayList<>());
        gd.playerManaPools.put(id, new ManaPool());
        gd.playerLifeTotals.put(id, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), id, "Charlie");
        return player;
    }
}
