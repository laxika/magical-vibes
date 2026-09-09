package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.k.KingCrab;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloatedToad.class, BouncingBeebles.class, KingCrab.class})
class BloatedToadTest extends BaseCardTest {

    @Test
    @DisplayName("Bloated Toad has protection from blue")
    void hasProtectionFromBlue() {
        Permanent toad = addCreatureReady(player1, new BloatedToad());

        assertThat(gqs.hasProtectionFrom(gd, toad, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, toad, CardColor.RED)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, toad, null)).isFalse();
    }

    @Test
    @DisplayName("A blue creature cannot block Bloated Toad")
    void blueCreatureCannotBlock() {
        Permanent toad = addCreatureReady(player1, new BloatedToad());
        toad.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BouncingBeebles());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("A blue ability cannot target Bloated Toad")
    void blueAbilityCannotTargetBloatedToad() {
        Permanent toad = addCreatureReady(player1, new BloatedToad());
        Permanent crab = addCreatureReady(player2, new KingCrab());

        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, toad.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from blue");
        assertThat(crab.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Combat damage from a blue creature is prevented by Bloated Toad protection")
    void preventsCombatDamageFromBlueCreature() {
        Permanent attacker = addCreatureReady(player2, new BouncingBeebles());
        attacker.setAttacking(true);
        Permanent toad = addCreatureReady(player1, new BloatedToad());
        toad.setBlocking(true);
        toad.addBlockingTarget(0);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(toad.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(toad);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card instanceof BouncingBeebles);
    }

    @Test
    @DisplayName("Cycling discards Bloated Toad and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new BloatedToad()));
        harness.setLibrary(player1, List.of(new BouncingBeebles()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Bloated Toad");
        harness.assertInHand(player1, "Bouncing Beebles");
    }

    @Test
    @DisplayName("Cycling Bloated Toad requires two generic mana")
    void cyclingRequiresTwoGenericMana() {
        BloatedToad toad = new BloatedToad();
        harness.setHand(player1, List.of(toad));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(toad);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
