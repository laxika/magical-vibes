package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RagingKavu;
import com.github.laxika.magicalvibes.cards.z.Zap;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TreefolkHealer.class, Forest.class, RagingKavu.class, Zap.class})
class TreefolkHealerTest extends BaseCardTest {

    private Permanent addHealerReady() {
        Permanent healer = addCreatureReady(player1, new TreefolkHealer());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        return healer;
    }

    private void castZapAt(UUID targetId) {
        harness.setHand(player1, List.of(new Zap()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    @Test
    @DisplayName("Prevents the next 2 damage to a target creature")
    void preventsNextDamageToCreature() {
        addHealerReady();
        Permanent target = addCreatureReady(player2, new RagingKavu());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getDamagePreventionShield()).isEqualTo(2);
    }

    @Test
    @DisplayName("Prevents the next 2 damage to a target player")
    void preventsNextDamageToPlayer() {
        addHealerReady();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("Prevents exactly the next 2 damage dealt to a creature")
    void preventsExactlyNextTwoDamageToCreature() {
        addHealerReady();
        Permanent target = addCreatureReady(player2, new RagingKavu());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        castZapAt(target.getId());
        castZapAt(target.getId());

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(target.getDamagePreventionShield()).isZero();

        castZapAt(target.getId());

        harness.assertNotOnBattlefield(player2, "Raging Kavu");
    }

    @Test
    @DisplayName("Prevents exactly the next 2 damage dealt to a player")
    void preventsExactlyNextTwoDamageToPlayer() {
        addHealerReady();
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        castZapAt(player2.getId());
        castZapAt(player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isZero();

        castZapAt(player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The prevention shield expires at the end of the turn")
    void shieldExpiresAtEndOfTurn() {
        addHealerReady();
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();

        castZapAt(player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Taps the healer and cannot be activated again while tapped")
    void tapsSourceAndRequiresUntappedSource() {
        Permanent healer = addHealerReady();
        Permanent target = addCreatureReady(player2, new RagingKavu());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(healer.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        addHealerReady();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without the mana cost")
    void requiresMana() {
        addCreatureReady(player1, new TreefolkHealer());
        Permanent target = addCreatureReady(player2, new RagingKavu());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
