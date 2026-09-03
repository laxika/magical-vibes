package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({Wellspring.class, Forest.class, BayFalcon.class})
class WellspringTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot enchant a creature")
    void cannotEnchantCreature() {
        addLand(player2);
        Permanent falcon = harness.addToBattlefieldAndReturn(player2, new BayFalcon());

        harness.setHand(player1, List.of(new Wellspring()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, falcon.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Entering gains control of the enchanted land until end of turn")
    void entersGainsControlOfLand() {
        Permanent land = addLand(player2);

        castWellspring(land);

        assertThat(controls(player1, land.getId())).isTrue();
        assertThat(controls(player2, land.getId())).isFalse();
    }

    @Test
    @DisplayName("Control of the land reverts to its owner at end of turn")
    void controlRevertsAtEndOfTurn() {
        Permanent land = addLand(player2);

        castWellspring(land);
        assertThat(controls(player1, land.getId())).isTrue();

        endTheTurn();

        assertThat(controls(player2, land.getId())).isTrue();
        assertThat(controls(player1, land.getId())).isFalse();
    }

    @Test
    @DisplayName("Upkeep trigger untaps the enchanted land and regains control of it")
    void upkeepUntapsAndRegainsControl() {
        Permanent land = addLand(player2);
        Permanent wellspring = harness.addToBattlefieldAndReturn(player1, new Wellspring());
        wellspring.setAttachedTo(land.getId());
        land.tap();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(land.isTapped()).isFalse();
        assertThat(controls(player1, land.getId())).isTrue();

        endTheTurn();

        assertThat(controls(player2, land.getId())).isTrue();
        assertThat(controls(player1, land.getId())).isFalse();
    }

    @Test
    @DisplayName("Upkeep trigger does not fire during the opponent's upkeep")
    void doesNotFireDuringOpponentUpkeep() {
        Permanent land = addLand(player2);
        Permanent wellspring = harness.addToBattlefieldAndReturn(player1, new Wellspring());
        wellspring.setAttachedTo(land.getId());
        land.tap();

        advanceToUpkeep(player2);
        land.tap();
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        assertThat(controls(player2, land.getId())).isTrue();
    }

    private void castWellspring(Permanent land) {
        harness.setHand(player1, List.of(new Wellspring()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void endTheTurn() {
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent addLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Forest());
    }

    private boolean controls(Player player, UUID permanentId) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .anyMatch(p -> p.getId().equals(permanentId));
    }
}
