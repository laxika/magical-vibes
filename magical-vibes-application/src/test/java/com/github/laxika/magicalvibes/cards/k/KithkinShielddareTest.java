package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.z.ZealousGuardian;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KithkinShielddare.class, ZealousGuardian.class})
class KithkinShielddareTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a blocking creature +2/+2 until end of turn")
    void boostsBlockingCreature() {
        setupShielddare();
        Permanent blocker = addBlockingGuardian(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isEqualTo(2);
        assertThat(blocker.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target an opponent's blocking creature")
    void boostsOpponentsBlockingCreature() {
        setupShielddare();
        Permanent blocker = addBlockingGuardian(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isEqualTo(2);
        assertThat(blocker.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOff() {
        setupShielddare();
        Permanent blocker = addBlockingGuardian(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isEqualTo(0);
        assertThat(blocker.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Taps the Shielddare when activated")
    void tapsOnActivation() {
        setupShielddare();
        Permanent blocker = addBlockingGuardian(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.activateAbility(player1, 0, null, blocker.getId());

        assertThat(findPermanent(player1, "Kithkin Shielddare").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature that is not blocking")
    void cannotTargetNonBlockingCreature() {
        setupShielddare();
        Permanent bystander = addCreatureReady(player1, new ZealousGuardian());
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bystander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blocking");
    }

    @Test
    @DisplayName("Does not boost a creature that stops blocking before resolution")
    void fizzlesIfTargetStopsBlocking() {
        setupShielddare();
        Permanent blocker = addBlockingGuardian(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.activateAbility(player1, 0, null, blocker.getId());
        blocker.setBlocking(false);
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isEqualTo(0);
        assertThat(blocker.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can boost itself while blocking even though activation taps it")
    void boostsItselfWhileBlocking() {
        setupShielddare();
        Permanent shielddare = findPermanent(player1, "Kithkin Shielddare");
        shielddare.setBlocking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.activateAbility(player1, 0, null, shielddare.getId());
        harness.passBothPriorities();

        assertThat(shielddare.isTapped()).isTrue();
        assertThat(shielddare.getPowerModifier()).isEqualTo(2);
        assertThat(shielddare.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability resolves after its source leaves the battlefield")
    void resolvesWithoutSource() {
        setupShielddare();
        Permanent blocker = addBlockingGuardian(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.activateAbility(player1, 0, null, blocker.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isEqualTo(2);
        assertThat(blocker.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        setupShielddare();
        findPermanent(player1, "Kithkin Shielddare").setSummoningSick(true);
        Permanent blocker = addBlockingGuardian(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, blocker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while already tapped")
    void cannotActivateWhileTapped() {
        setupShielddare();
        findPermanent(player1, "Kithkin Shielddare").setTapped(true);
        Permanent blocker = addBlockingGuardian(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, blocker.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without white mana")
    void cannotActivateWithoutWhiteMana() {
        addCreatureReady(player1, new KithkinShielddare());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        Permanent blocker = addBlockingGuardian(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, blocker.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Kithkin Shielddare").isTapped()).isFalse();
    }

    private void setupShielddare() {
        addCreatureReady(player1, new KithkinShielddare());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
    }

    private Permanent addBlockingGuardian(Player player) {
        Permanent blocker = addCreatureReady(player, new ZealousGuardian());
        blocker.setBlocking(true);
        return blocker;
    }
}
