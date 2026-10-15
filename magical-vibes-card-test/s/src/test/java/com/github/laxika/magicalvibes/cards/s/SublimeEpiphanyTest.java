package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SublimeEpiphany.class, GrizzlyBears.class, RodOfRuin.class, Spellbook.class,
        Mountain.class, Skyscanner.class})
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
        harness.castModalInstantWithModes(player2, 0, 1, 5, new int[]{0}, bears.getId(), List.of());
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
        harness.castModalInstantWithModes(player1, 0, 1, 5, new int[]{1}, rod.getId(), List.of());
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

    @Test
    void bounceAndCopySameCreatureUsesLastKnownCopiableValues() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Skyscanner());
        harness.setLibrary(player1, List.of(new Mountain()));
        addSublimeEpiphany(player1);

        harness.castModalInstantWithModes(player1, 0, 1, 5, new int[]{2, 3},
                List.of(creature.getId(), creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Skyscanner");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Skyscanner"))
                .hasSize(1);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Mountain");
    }

    @Test
    void abilityModeCountersTriggeredAbility() {
        harness.setHand(player2, List.of(new Skyscanner()));
        harness.setLibrary(player2, List.of(new Mountain()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        addSublimeEpiphany(player1);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        var abilityId = gd.stack.getLast().getTargetableId();

        harness.castModalInstantWithModes(player1, 0, 1, 5, new int[]{1}, abilityId, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Skyscanner");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copyModeRejectsOpponentCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Skyscanner());
        addSublimeEpiphany(player1);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(player1, 0, 1, 5,
                new int[]{3}, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void illegalCopyTargetDoesNotPreventDrawForLegalPlayerTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Skyscanner());
        harness.setLibrary(player1, List.of(new Mountain()));
        addSublimeEpiphany(player1);
        addSublimeEpiphany(player2);

        harness.castModalInstantWithModes(player1, 0, 1, 5, new int[]{3, 4},
                List.of(creature.getId(), player1.getId()));
        harness.castModalInstantWithModes(player2, 0, 1, 5, new int[]{2},
                List.of(creature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Skyscanner");
        harness.assertInHand(player1, "Mountain");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void counterSpellAndAbilityModesUseSeparateStackTargets() {
        RodOfRuin rod = new RodOfRuin();
        harness.addToBattlefield(player2, rod);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        addSublimeEpiphany(player1);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.activateAbility(player2, 0, null, player1.getId());
        var abilityId = gd.stack.getLast().getTargetableId();

        harness.castModalInstantWithModes(player1, 0, 1, 5, new int[]{0, 1},
                List.of(bears.getId(), abilityId));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }
}
