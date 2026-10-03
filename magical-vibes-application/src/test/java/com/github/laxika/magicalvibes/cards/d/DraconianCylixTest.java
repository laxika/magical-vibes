package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.t.Thallid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DraconianCylix.class, Thallid.class})
class DraconianCylixTest extends BaseCardTest {

    @Test
    void activatesByTappingAndDiscardingAtRandomThenRegeneratesTargetCreature() {
        Permanent cylix = harness.addToBattlefieldAndReturn(player1, new DraconianCylix());
        Permanent thallid = addCreatureReady(player2, new Thallid());
        harness.setHand(player1, List.of(new Thallid()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, thallid.getId());

        assertThat(cylix.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Thallid");

        harness.passBothPriorities();

        assertThat(thallid.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void cannotActivateWithAnEmptyHand() {
        Permanent cylix = harness.addToBattlefieldAndReturn(player1, new DraconianCylix());
        Permanent thallid = addCreatureReady(player1, new Thallid());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, thallid.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(cylix.isTapped()).isFalse();
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent cylix = harness.addToBattlefieldAndReturn(player1, new DraconianCylix());
        harness.setHand(player1, List.of(new Thallid()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, cylix.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void regenerationShieldPreventsLethalCombatDamage() {
        harness.addToBattlefield(player1, new DraconianCylix());
        Permanent target = addCreatureReady(player1, new Thallid());
        harness.setHand(player1, List.of(new Thallid()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player2, new Thallid());
        attacker.setAttacking(true);
        target.setBlocking(true);
        target.addBlockingTarget(0);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Thallid");
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getRegenerationShield()).isZero();
    }

    @Test
    void discardsExactlyOneCardFromAMultipleCardHandBeforeResolution() {
        harness.addToBattlefield(player1, new DraconianCylix());
        Permanent target = addCreatureReady(player1, new Thallid());
        harness.setHand(player1, List.of(new Thallid(), new DraconianCylix()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()).getFirst().getName())
                .isIn("Thallid", "Draconian Cylix");
        assertThat(target.getRegenerationShield()).isZero();

        harness.passBothPriorities();

        assertThat(target.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent cylix = harness.addToBattlefieldAndReturn(player1, new DraconianCylix());
        cylix.setTapped(true);
        Permanent target = addCreatureReady(player1, new Thallid());
        harness.setHand(player1, List.of(new Thallid()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(target.getRegenerationShield()).isZero();
    }

    @Test
    void cannotActivateWithoutEnoughMana() {
        Permanent cylix = harness.addToBattlefieldAndReturn(player1, new DraconianCylix());
        Permanent target = addCreatureReady(player1, new Thallid());
        harness.setHand(player1, List.of(new Thallid()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(cylix.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(target.getRegenerationShield()).isZero();
    }
}
