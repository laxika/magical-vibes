package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.s.SkyEelSchool;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WallOfTanglecord.class, SkyEelSchool.class})
class WallOfTanglecordTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Wall of Tanglecord puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new WallOfTanglecord()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Wall of Tanglecord");
    }

    @Test
    @DisplayName("Resolving puts Wall of Tanglecord onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new WallOfTanglecord()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Wall of Tanglecord");
    }

    @Test
    @DisplayName("Activating reach ability puts it on the stack")
    void activatingReachPutsOnStack() {
        Permanent wall = addCreatureReady(player1, new WallOfTanglecord());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Wall of Tanglecord");
        assertThat(entry.getTargetId()).isEqualTo(wall.getId());
    }

    @Test
    @DisplayName("Resolving reach ability grants reach until end of turn")
    void resolvingReachAbilityGrantsReach() {
        Permanent wall = addCreatureReady(player1, new WallOfTanglecord());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, wall, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Reach granted by ability resets at end of turn cleanup")
    void reachResetsAtEndOfTurn() {
        Permanent wall = addCreatureReady(player1, new WallOfTanglecord());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wall, Keyword.REACH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wall, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Activating ability does NOT tap Wall of Tanglecord")
    void activatingAbilityDoesNotTap() {
        Permanent wall = addCreatureReady(player1, new WallOfTanglecord());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(wall.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate ability without green mana")
    void cannotActivateWithoutGreenMana() {
        addCreatureReady(player1, new WallOfTanglecord());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can activate ability when tapped")
    void canActivateWhenTapped() {
        Permanent wall = addCreatureReady(player1, new WallOfTanglecord());
        wall.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Wall of Tanglecord");
    }

    @Test
    @DisplayName("Can activate ability with summoning sickness")
    void canActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new WallOfTanglecord());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Wall of Tanglecord");
    }

    @Test
    @DisplayName("Wall of Tanglecord with reach can block a flying creature")
    void canBlockFlyingWithReach() {
        addCreatureReady(player1, new SkyEelSchool());
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfTanglecord());
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(wall.isBlocking()).isTrue();
    }

    @Test
    void cannotBlockFlyingWithoutReach() {
        addCreatureReady(player1, new SkyEelSchool());
        harness.addToBattlefield(player2, new WallOfTanglecord());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void defenderPreventsAttackingEvenWithReach() {
        addCreatureReady(player1, new WallOfTanglecord());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void reachIsGrantedOnlyOnResolutionAndOnlyToTheSource() {
        Permanent wall = addCreatureReady(player1, new WallOfTanglecord());
        Permanent otherWall = addCreatureReady(player1, new WallOfTanglecord());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gqs.hasKeyword(gd, wall, Keyword.REACH)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, wall, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherWall, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Ability resolves without granting reach when its source has left the battlefield")
    void abilityDoesNothingIfSourceRemoved() {
        addCreatureReady(player1, new WallOfTanglecord());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityDoesNotGrantReachToReturnedSource() {
        WallOfTanglecord card = new WallOfTanglecord();
        Permanent original = addCreatureReady(player1, card);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(original);
        Permanent returned = harness.addToBattlefieldAndReturn(player1, card);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.REACH)).isFalse();
    }

}
