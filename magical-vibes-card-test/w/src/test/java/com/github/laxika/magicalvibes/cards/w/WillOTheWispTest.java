package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WillOTheWisp.class, GrizzlyBears.class})
class WillOTheWispTest extends BaseCardTest {
    @Test
    void activatingAbilityDoesNotTap() {
        Permanent wisp = addCreatureReady(player1, new WillOTheWisp());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        assertThat(wisp.isTapped()).isFalse();
    }

    @Test
    void activatingAbilityConsumesBlackMana() {
        addCreatureReady(player1, new WillOTheWisp());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void cannotActivateWithoutBlackMana() {
        addCreatureReady(player1, new WillOTheWisp());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activating regeneration ability puts it on the stack with self as target")
    void activatingAbilityPutsOnStack() {
        Permanent wisp = addCreatureReady(player1, new WillOTheWisp());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(wisp.getId());
    }

    @Test
    @DisplayName("Resolving regeneration ability grants a regeneration shield")
    void resolvingAbilityGrantsShield() {
        addCreatureReady(player1, new WillOTheWisp());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent wisp = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(wisp.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void flyingPreventsGroundBlocker() {
        addCreatureReady(player1, new WillOTheWisp());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(player1, List.of(0));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Regeneration shield saves Will-o'-the-Wisp from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        addCreatureReady(player1, new WillOTheWisp()).setRegenerationShield(1);
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        Permanent survivor = findPermanent(player1, "Will-o'-the-Wisp");
        assertThat(survivor.isTapped()).isTrue();
        assertThat(survivor.getRegenerationShield()).isEqualTo(0);
        assertThat(survivor.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Will-o'-the-Wisp dies in combat without a regeneration shield")
    void diesWithoutShield() {
        addCreatureReady(player1, new WillOTheWisp());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Will-o'-the-Wisp");
        harness.assertInGraveyard(player1, "Will-o'-the-Wisp");
    }

    @Test
    void canRegenerateWhileTappedAndSummoningSick() {
        Permanent wisp = addCreatureReady(player1, new WillOTheWisp());
        wisp.setSummoningSick(true);
        wisp.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wisp.getRegenerationShield()).isEqualTo(1);
        assertThat(wisp.isTapped()).isTrue();
    }

    @Test
    void resolvingAbilityDoesNotTapOrRemoveCreatureFromCombat() {
        Permanent wisp = addCreatureReady(player1, new WillOTheWisp());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 2);
        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wisp.getRegenerationShield()).isEqualTo(1);
        assertThat(wisp.isTapped()).isFalse();
        assertThat(wisp.isBlocking()).isTrue();

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Will-o'-the-Wisp");
        assertThat(wisp.getRegenerationShield()).isZero();
        assertThat(wisp.isTapped()).isTrue();
        assertThat(wisp.isBlocking()).isFalse();
        assertThat(wisp.getMarkedDamage()).isZero();
    }

    @Test
    void multipleActivationsGrantIndependentShields() {
        Permanent wisp = addCreatureReady(player1, new WillOTheWisp());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wisp.getRegenerationShield()).isEqualTo(2);
        assertThat(wisp.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
