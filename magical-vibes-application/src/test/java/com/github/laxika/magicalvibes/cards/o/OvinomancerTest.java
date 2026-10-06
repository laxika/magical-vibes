package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FlyingMen;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Pendelhaven;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.StuffyDoll;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Ovinomancer.class, Forest.class, Island.class, Plains.class, Pendelhaven.class,
        FlyingMen.class, StuffyDoll.class})
class OvinomancerTest extends BaseCardTest {

    private long basicLandsControlledBy(UUID playerId) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND)
                        && p.getCard().getSupertypes().contains(CardSupertype.BASIC))
                .count();
    }

    private int battlefieldIndex(Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).indexOf(findPermanent(owner, name));
    }

    private void castOvinomancer() {
        harness.castFromHand(player1, new Ovinomancer(), "{2}{U}");
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Auto-sacrifices when controller has fewer than three basic lands")
    void autoSacrificesWithoutThreeBasicLands() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        castOvinomancer();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Ovinomancer");
        harness.assertInGraveyard(player1, "Ovinomancer");
        assertThat(basicLandsControlledBy(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not count nonbasic lands toward the three-land requirement")
    void doesNotCountNonbasicLands() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Pendelhaven());
        castOvinomancer();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Ovinomancer");
        harness.assertInGraveyard(player1, "Ovinomancer");
        harness.assertOnBattlefield(player1, "Pendelhaven");
        assertThat(basicLandsControlledBy(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Prompts a may ability when controller has three or more basic lands")
    void promptsMayAbilityWithThreeBasicLands() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Forest());
        castOvinomancer();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting with exactly three basic lands returns them and keeps Ovinomancer")
    void acceptWithExactlyThreeBasicLands() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Forest());
        castOvinomancer();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(basicLandsControlledBy(player1.getId())).isEqualTo(0);
        assertThat(gd.playerHands.get(player1.getId()).stream()
                .filter(c -> c.hasType(CardType.LAND)).count()).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Ovinomancer");
    }

    @Test
    @DisplayName("Accepting with four basic lands lets controller choose which three to return")
    void acceptWithFourBasicLandsChoosesThree() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Forest());
        castOvinomancer();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);

        List<UUID> landIds = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .map(Permanent::getId)
                .limit(3)
                .toList();
        harness.handleMultiplePermanentsChosen(player1, landIds);

        assertThat(basicLandsControlledBy(player1.getId())).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Ovinomancer");
    }

    @Test
    @DisplayName("Declining sacrifices Ovinomancer and keeps the lands")
    void declineSacrificesOvinomancer() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Forest());
        castOvinomancer();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Ovinomancer");
        harness.assertInGraveyard(player1, "Ovinomancer");
        assertThat(basicLandsControlledBy(player1.getId())).isEqualTo(3);
    }

    @Test
    @DisplayName("Activated ability destroys target and gives its controller a Sheep token")
    void activatedAbilityDestroysAndCreatesSheep() {
        addCreatureReady(player1, new Ovinomancer());
        harness.addToBattlefield(player2, new FlyingMen());
        UUID targetId = harness.getPermanentId(player2, "Flying Men");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, battlefieldIndex(player1, "Ovinomancer"), null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ovinomancer");
        harness.assertInHand(player1, "Ovinomancer");
        harness.assertNotOnBattlefield(player2, "Flying Men");
        harness.assertInGraveyard(player2, "Flying Men");

        Permanent sheep = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Sheep"))
                .findFirst().orElseThrow();
        assertThat(sheep.getCard().getPower()).isEqualTo(0);
        assertThat(sheep.getCard().getToughness()).isEqualTo(1);
        assertThat(sheep.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(sheep.getCard().getSubtypes()).contains(CardSubtype.SHEEP);
    }

    @Test
    @DisplayName("Target creature can't be regenerated by the activated ability")
    void activatedAbilityIgnoresRegeneration() {
        addCreatureReady(player1, new Ovinomancer());
        harness.addToBattlefield(player2, new FlyingMen());
        Permanent flyingMen = findPermanent(player2, "Flying Men");
        UUID targetId = flyingMen.getId();
        flyingMen.setRegenerationShield(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, battlefieldIndex(player1, "Ovinomancer"), null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Flying Men");
        harness.assertInGraveyard(player2, "Flying Men");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        addCreatureReady(player1, new Ovinomancer());
        Permanent pendelhaven = harness.addToBattlefieldAndReturn(player2, new Pendelhaven());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Ovinomancer"), null, pendelhaven.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");

        harness.assertOnBattlefield(player1, "Ovinomancer");
        harness.assertOnBattlefield(player2, "Pendelhaven");
    }

    @Test
    @DisplayName("Returns to its owner's hand when controlled by another player")
    void returnsToOwnersHandWhenControlledByAnotherPlayer() {
        Ovinomancer ovinomancer = new Ovinomancer();
        ovinomancer.setOwnerId(player1.getId());
        addCreatureReady(player2, ovinomancer);
        Permanent flyingMen = addCreatureReady(player1, new FlyingMen());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player2, battlefieldIndex(player2, "Ovinomancer"), null, flyingMen.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ovinomancer");
        harness.assertInHand(player1, "Ovinomancer");
        harness.assertNotInHand(player2, "Ovinomancer");
        harness.assertNotOnBattlefield(player1, "Flying Men");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().isToken() && p.getCard().getName().equals("Sheep"))).isTrue();
    }

    @Test
    @DisplayName("Self-targeting fails after returning Ovinomancer as the activation cost")
    void selfTargetBecomesIllegalAfterReturningOvinomancer() {
        addCreatureReady(player1, new Ovinomancer());
        UUID targetId = harness.getPermanentId(player1, "Ovinomancer");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, battlefieldIndex(player1, "Ovinomancer"), null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ovinomancer");
        harness.assertInHand(player1, "Ovinomancer");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .noneMatch(p -> p.getCard().isToken() && p.getCard().getName().equals("Sheep"))).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .noneMatch(p -> p.getCard().isToken() && p.getCard().getName().equals("Sheep"))).isTrue();
    }

    @Test
    @DisplayName("An indestructible target survives and its controller still creates a Sheep")
    void indestructibleTargetStillCreatesSheep() {
        addCreatureReady(player1, new Ovinomancer());
        harness.addToBattlefield(player2, new StuffyDoll());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, battlefieldIndex(player1, "Ovinomancer"), null,
                harness.getPermanentId(player2, "Stuffy Doll"));

        harness.assertInHand(player1, "Ovinomancer");
        assertThat(countPermanents(player2, "Sheep")).isZero();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Stuffy Doll");
        assertThat(countPermanents(player2, "Sheep")).isEqualTo(1);
        assertThat(countPermanents(player1, "Sheep")).isZero();
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost and leaves Ovinomancer in play")
    void summoningSicknessPreventsActivation() {
        harness.addToBattlefield(player1, new Ovinomancer());
        harness.addToBattlefield(player2, new FlyingMen());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                battlefieldIndex(player1, "Ovinomancer"), null,
                harness.getPermanentId(player2, "Flying Men")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        harness.assertOnBattlefield(player1, "Ovinomancer");
        harness.assertNotInHand(player1, "Ovinomancer");
        harness.assertOnBattlefield(player2, "Flying Men");
    }

    @Test
    @DisplayName("Tapped basic lands can be returned, including a land owned by the opponent")
    void returnsTappedBasicLandsToTheirOwners() {
        Island borrowedIsland = new Island();
        borrowedIsland.setOwnerId(player2.getId());
        harness.addToBattlefieldAndReturn(player1, borrowedIsland).tap();
        harness.addToBattlefieldAndReturn(player1, new Plains()).tap();
        harness.addToBattlefieldAndReturn(player1, new Forest()).tap();
        castOvinomancer();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Ovinomancer");
        assertThat(basicLandsControlledBy(player1.getId())).isZero();
        harness.assertInHand(player2, "Island");
        harness.assertNotInHand(player1, "Island");
        harness.assertInHand(player1, "Plains");
        harness.assertInHand(player1, "Forest");
    }
}
