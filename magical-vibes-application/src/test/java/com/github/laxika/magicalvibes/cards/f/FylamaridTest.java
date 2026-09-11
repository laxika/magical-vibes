package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Fylamarid.class, FightingDrake.class, Firefly.class, FlowstoneGiant.class})
class FylamaridTest extends BaseCardTest {

    @Test
    @DisplayName("Fylamarid can't be blocked by a blue creature")
    void cannotBeBlockedByBlueCreature() {
        Permanent blocker = attackSetup(new FightingDrake());

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fylamarid can be blocked by a non-blue creature")
    void canBeBlockedByNonBlueCreature() {
        Permanent blocker = attackSetup(new Firefly());

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Fylamarid can't be blocked by a non-flying creature")
    void cannotBeBlockedByNonFlyingCreature() {
        Permanent blocker = attackSetup(new FlowstoneGiant());

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    @DisplayName("{U}: target creature becomes blue, replacing its other colors")
    void targetBecomesBlue() {
        harness.addToBattlefield(player1, new Fylamarid());
        harness.addToBattlefield(player2, new FlowstoneGiant());
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID giantId = harness.getPermanentId(player2, "Flowstone Giant");
        harness.activateAbility(player1, 0, 0, null, giantId);
        harness.passBothPriorities();

        Permanent giant = findPermanent(player2, "Flowstone Giant");
        assertThat(gqs.getEffectiveColors(gd, giant)).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("Blue wears off at end of turn")
    void blueWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new Fylamarid());
        harness.addToBattlefield(player1, new FlowstoneGiant());
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID giantId = harness.getPermanentId(player1, "Flowstone Giant");
        harness.activateAbility(player1, 0, 0, null, giantId);
        harness.passBothPriorities();

        Permanent giant = findPermanent(player1, "Flowstone Giant");
        assertThat(gqs.getEffectiveColors(gd, giant)).containsExactly(CardColor.BLUE);

        gd.expireEndOfTurnFloatingEffects();
        giant.resetModifiers();

        assertThat(gqs.getEffectiveColors(gd, giant)).containsExactly(CardColor.RED);
    }

    /**
     * Puts an attacking Fylamarid on player1's battlefield and the given blocker on player2's,
     * then advances to the declare-blockers input state. Returns the blocker permanent.
     */
    private Permanent attackSetup(com.github.laxika.magicalvibes.model.Card blockerCard) {
        Permanent blocker = addCreatureReady(player2, blockerCard);
        Permanent attacker = addCreatureReady(player1, new Fylamarid());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);
        return blocker;
    }
}
