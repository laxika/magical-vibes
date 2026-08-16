package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SublimeEpiphanyTest extends BaseCardTest {

    private void addSublimeEpiphany(Player player) {
        harness.setHand(player, List.of(new SublimeEpiphany()));
        harness.addMana(player, ManaColor.BLUE, 6);
    }

    @Test
    @DisplayName("Counter mode counters a target spell")
    void counterSpellMode() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        addSublimeEpiphany(player2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        castWithSpellTarget(player2, new int[]{0}, bears.getId(), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Ability mode counters an activated ability")
    void counterAbilityMode() {
        RodOfRuin rod = new RodOfRuin();
        harness.addToBattlefield(player2, rod);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        addSublimeEpiphany(player1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        castWithSpellTarget(player1, new int[]{1}, rod.getId(), List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Bounce, copy, and draw modes resolve with their targets")
    void bounceCopyAndDrawModes() {
        Permanent bounced = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        Permanent copied = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        addSublimeEpiphany(player1);

        harness.castModalInstantWithModes(player1, 0, 1, 5, new int[]{2, 3, 4},
                List.of(bounced.getId(), copied.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Spellbook");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Grizzly Bears"));
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Bounce mode cannot target a land")
    void bounceModeRejectsLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        addSublimeEpiphany(player1);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 5, new int[]{2}, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castWithSpellTarget(Player player, int[] modeIndices, java.util.UUID targetId,
                                     List<java.util.UUID> targetIds) {
        harness.getGameService().playCard(gd, player, 0,
                ChooseOneEffect.encodeModeSelection(1, 5, modeIndices), targetId, null, targetIds, List.of());
    }
}
