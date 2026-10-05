package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DismantlingBlow;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KavuAggressor;
import com.github.laxika.magicalvibes.cards.r.Repulse;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({MeteorStorm.class, Forest.class, Mountain.class, KavuAggressor.class,
        DismantlingBlow.class, Repulse.class})
class MeteorStormTest extends BaseCardTest {

    @Test
    @DisplayName("Ability deals 4 damage to a creature and discards two cards at random as costs")
    void damagesCreatureAndDiscardsTwoCardsAtRandom() {
        harness.addToBattlefield(player1, new MeteorStorm());
        harness.addToBattlefield(player2, new KavuAggressor());
        harness.setHand(player1, List.of(new Forest(), new Mountain()));
        addActivationMana();

        UUID kavuId = harness.getPermanentId(player2, "Kavu Aggressor");
        harness.activateAbility(player1, battlefieldIndex(player1, "Meteor Storm"), null, kavuId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Kavu Aggressor");
        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Forest", "Mountain");
    }

    @Test
    @DisplayName("Ability discards exactly two cards when more than two are in hand")
    void discardsExactlyTwoCards() {
        harness.addToBattlefield(player1, new MeteorStorm());
        harness.setHand(player1, List.of(new Forest(), new Mountain(), new Forest()));
        harness.setLife(player2, 20);
        addActivationMana();

        harness.activateAbility(player1, battlefieldIndex(player1, "Meteor Storm"), null, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(1);
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Ability deals 4 damage to a player")
    void damagesPlayer() {
        harness.addToBattlefield(player1, new MeteorStorm());
        harness.setHand(player1, List.of(new Forest(), new Mountain()));
        harness.setLife(player2, 20);
        addActivationMana();

        harness.activateAbility(player1, battlefieldIndex(player1, "Meteor Storm"), null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Ability can target its controller")
    void damagesController() {
        harness.addToBattlefield(player1, new MeteorStorm());
        harness.setHand(player1, List.of(new Forest(), new Mountain()));
        harness.setLife(player1, 20);
        addActivationMana();

        harness.activateAbility(player1, battlefieldIndex(player1, "Meteor Storm"), null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
    }

    @Test
    @DisplayName("Cannot activate with fewer than two cards in hand")
    void cannotActivateWithFewerThanTwoCards() {
        harness.addToBattlefield(player1, new MeteorStorm());
        harness.setHand(player1, List.of(new Forest()));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Meteor Storm"), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without choosing an any target")
    void cannotActivateWithoutTarget() {
        harness.addToBattlefield(player1, new MeteorStorm());
        harness.setHand(player1, List.of(new Forest(), new Mountain()));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Meteor Storm"), null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(harness.getGameData().playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest", "Mountain");
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotalAllMana())
                .isEqualTo(4);
    }

    @Test
    @DisplayName("Mana and random discards are paid before damage resolves")
    void paysCostsBeforeResolution() {
        harness.addToBattlefield(player1, new MeteorStorm());
        harness.setHand(player1, List.of(new Forest(), new Mountain()));
        harness.setLife(player2, 20);
        addActivationMana();

        harness.activateAbility(player1, battlefieldIndex(player1, "Meteor Storm"), null, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Mountain");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Missing green mana prevents activation without discarding cards")
    void cannotActivateWithoutGreenMana() {
        harness.addToBattlefield(player1, new MeteorStorm());
        harness.setHand(player1, List.of(new Forest(), new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Meteor Storm"), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability can be activated twice without tapping the enchantment")
    void canActivateTwiceBeforeResolution() {
        harness.addToBattlefield(player1, new MeteorStorm());
        harness.setHand(player1, List.of(new Forest(), new Mountain(), new Forest(), new Mountain()));
        harness.setLife(player2, 20);
        addActivationMana();
        addActivationMana();

        harness.activateAbility(player1, battlefieldIndex(player1, "Meteor Storm"), null, player2.getId());
        harness.activateAbility(player1, battlefieldIndex(player1, "Meteor Storm"), null, player2.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        harness.assertLife(player2, 20);

        resolveAllTriggers();

        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("Destroying Meteor Storm in response does not stop its activated ability")
    void resolvesAfterSourceIsDestroyed() {
        UUID sourceId = harness.addToBattlefieldAndReturn(player1, new MeteorStorm()).getId();
        harness.setHand(player1, List.of(new Forest(), new Mountain()));
        harness.setHand(player2, List.of(new DismantlingBlow()));
        harness.setLife(player2, 20);
        addActivationMana();
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, "Meteor Storm"), null, player2.getId());
        harness.castAndResolveInstant(player2, 0, sourceId);

        harness.assertInGraveyard(player1, "Meteor Storm");
        harness.assertLife(player2, 20);

        resolveAllTriggers();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Returning the target to hand in response prevents damage without refunding costs")
    void targetLeavingDoesNotRefundCosts() {
        harness.addToBattlefield(player1, new MeteorStorm());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new KavuAggressor()).getId();
        harness.setHand(player1, List.of(new Forest(), new Mountain()));
        harness.setHand(player2, List.of(new Repulse()));
        harness.setLibrary(player2, List.of(new Forest(), new Mountain()));
        harness.setLife(player2, 20);
        addActivationMana();
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, "Meteor Storm"), null, targetId);
        harness.castAndResolveInstant(player2, 0, targetId);
        resolveAllTriggers();

        harness.assertInHand(player2, "Kavu Aggressor");
        harness.assertNotInGraveyard(player2, "Kavu Aggressor");
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A land is not a legal target and an invalid target does not consume costs")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new MeteorStorm());
        UUID landId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();
        harness.setHand(player1, List.of(new Forest(), new Mountain()));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Meteor Storm"), null, landId))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability can be activated during an opponent's upkeep")
    void canActivateDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new MeteorStorm());
        harness.setHand(player1, List.of(new Forest(), new Mountain()));
        harness.setLife(player2, 20);
        addActivationMana();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateAbility(player1, battlefieldIndex(player1, "Meteor Storm"), null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private int battlefieldIndex(Player player, String cardName) {
        return gd.playerBattlefields.get(player.getId()).indexOf(findPermanent(player, cardName));
    }
}
