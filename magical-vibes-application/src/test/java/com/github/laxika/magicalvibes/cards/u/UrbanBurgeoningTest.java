package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UrbanBurgeoningTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot enchant a creature")
    void cannotEnchantCreature() {
        addLand(player1);
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.setHand(player1, List.of(new UrbanBurgeoning()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Resolving Urban Burgeoning attaches it to the target land")
    void resolvingAttachesToLand() {
        Permanent land = addLand(player1);

        harness.setHand(player1, List.of(new UrbanBurgeoning()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Urban Burgeoning")
                        && p.isAttached()
                        && p.getAttachedTo().equals(land.getId()));
    }

    @Test
    @DisplayName("Enchanted land untaps during another player's untap step")
    void enchantedLandUntapsDuringOtherPlayersUntapStep() {
        Permanent land = addLand(player1);
        attachAura(land);
        land.tap();

        advanceToNextTurn(player1);

        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Other lands the controller controls stay tapped")
    void otherLandsStayTapped() {
        Permanent enchanted = addLand(player1);
        Permanent other = addLand(player1);
        attachAura(enchanted);
        enchanted.tap();
        other.tap();

        advanceToNextTurn(player1);

        assertThat(enchanted.isTapped()).isFalse();
        assertThat(other.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Without the aura, a non-active player's land stays tapped")
    void withoutAuraLandStaysTapped() {
        Permanent land = addLand(player1);
        land.tap();

        advanceToNextTurn(player1);

        assertThat(land.isTapped()).isTrue();
    }

    private void attachAura(Permanent land) {
        Permanent aura = new Permanent(new UrbanBurgeoning());
        aura.setAttachedTo(land.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
    }

    private Permanent addLand(Player player) {
        Permanent perm = new Permanent(new Forest());
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
