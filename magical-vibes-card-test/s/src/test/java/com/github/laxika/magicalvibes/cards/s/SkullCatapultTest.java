package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EkunduGriffin;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkullCatapult.class, EkunduGriffin.class})
class SkullCatapultTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to target player; taps and sacrifices a creature as cost")
    void deals2DamageToPlayer() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new SkullCatapult());
        addCreatureReady(player1, new EkunduGriffin()); // sacrifice fodder
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 18);
        assertThat(findPermanent(player1, "Skull Catapult").isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Ekundu Griffin");
    }

    @Test
    @DisplayName("Deals 2 damage to target creature, killing a 2/2")
    void deals2DamageKillingCreature() {
        harness.addToBattlefield(player1, new SkullCatapult());
        addCreatureReady(player1, new EkunduGriffin()); // sacrifice fodder
        harness.addToBattlefield(player2, new EkunduGriffin()); // 2/2 victim
        UUID victim = harness.getPermanentId(player2, "Ekundu Griffin");
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, victim);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Ekundu Griffin");
    }

    @Test
    @DisplayName("Cannot activate without paying the generic mana cost")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new SkullCatapult());
        addCreatureReady(player1, new EkunduGriffin());
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanent(player1, "Skull Catapult").isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Ekundu Griffin");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without a creature to sacrifice")
    void cannotActivateWithoutCreature() {
        harness.addToBattlefield(player1, new SkullCatapult());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanent(player1, "Skull Catapult").isTapped()).isFalse();
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without choosing a damage target")
    void cannotActivateWithoutTarget() {
        harness.addToBattlefield(player1, new SkullCatapult());
        addCreatureReady(player1, new EkunduGriffin());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanent(player1, "Skull Catapult").isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Ekundu Griffin");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new SkullCatapult());
        addCreatureReady(player1, new EkunduGriffin());
        UUID nonTargetPermanent = harness.getPermanentId(player1, "Skull Catapult");
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonTargetPermanent))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanent(player1, "Skull Catapult").isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Ekundu Griffin");
        harness.assertLife(player2, 20);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate a tapped Skull Catapult")
    void cannotActivateWhenTapped() {
        harness.addToBattlefieldAndReturn(player1, new SkullCatapult()).tap();
        harness.addToBattlefield(player1, new EkunduGriffin());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Ekundu Griffin");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped, newly entered creature can be sacrificed and damage can target you")
    void sacrificesTappedCreatureToDamageController() {
        harness.addToBattlefield(player1, new SkullCatapult());
        harness.addToBattlefieldAndReturn(player1, new EkunduGriffin()).tap();
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player1.getId());

        harness.assertInGraveyard(player1, "Ekundu Griffin");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("The target creature may be sacrificed to pay the cost, leaving an illegal target")
    void canSacrificeTargetCreature() {
        harness.addToBattlefield(player1, new SkullCatapult());
        harness.addToBattlefield(player1, new EkunduGriffin());
        UUID target = harness.getPermanentId(player1, "Ekundu Griffin");
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target);

        harness.assertInGraveyard(player1, "Ekundu Griffin");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        harness.addToBattlefield(player1, new SkullCatapult());
        harness.addToBattlefield(player2, new EkunduGriffin());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Ekundu Griffin");
        assertThat(findPermanent(player1, "Skull Catapult").isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
