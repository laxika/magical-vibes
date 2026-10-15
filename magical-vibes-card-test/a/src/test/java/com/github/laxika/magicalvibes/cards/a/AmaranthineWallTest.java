package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.cards.c.CastDown;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AmaranthineWall.class, CastDown.class})
class AmaranthineWallTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Amaranthine Wall puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new AmaranthineWall()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Amaranthine Wall");
    }

    @Test
    @DisplayName("Resolving puts Amaranthine Wall onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new AmaranthineWall()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Amaranthine Wall");
    }

    @Test
    @DisplayName("Activating indestructible ability puts it on the stack")
    void activatingIndestructiblePutsOnStack() {
        Permanent wall = addWallReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Amaranthine Wall");
        assertThat(entry.getTargetId()).isEqualTo(wall.getId());
    }

    @Test
    @DisplayName("Resolving indestructible ability grants indestructible until end of turn")
    void resolvingGrantsIndestructible() {
        Permanent wall = addWallReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, wall, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Indestructible granted by ability resets at end of turn cleanup")
    void indestructibleResetsAtEndOfTurn() {
        Permanent wall = addWallReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wall, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wall, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Activating ability does NOT tap Amaranthine Wall")
    void activatingAbilityDoesNotTap() {
        Permanent wall = addWallReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(wall.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addWallReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can activate ability when tapped")
    void canActivateWhenTapped() {
        Permanent wall = addWallReady(player1);
        wall.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Amaranthine Wall");
    }

    @Test
    @DisplayName("Can activate ability with summoning sickness (no tap required)")
    void canActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new AmaranthineWall());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Amaranthine Wall");
    }

    @Test
    @DisplayName("Can activate ability multiple times in same turn")
    void canActivateMultipleTimes() {
        Permanent wall = addWallReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wall, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Ability resolves without granting indestructible if its source has left the battlefield")
    void abilityHasNoEffectIfSourceRemoved() {
        addWallReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Indestructible protects the Wall from Cast Down")
    void indestructiblePreventsDestruction() {
        Permanent wall = addWallReady(player1);
        Permanent otherWall = harness.addToBattlefieldAndReturn(player1, new AmaranthineWall());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, otherWall, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.setHand(player2, List.of(new CastDown()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, wall.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wall);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Destroying the Wall in response prevents its ability from protecting it")
    void destructionInResponseIsNotPrevented() {
        Permanent wall = addWallReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.setHand(player2, List.of(new CastDown()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, wall.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Amaranthine Wall");
        Permanent replacement = harness.addToBattlefieldAndReturn(player1, new AmaranthineWall());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, replacement, Keyword.INDESTRUCTIBLE)).isFalse();
    }
    private Permanent addWallReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new AmaranthineWall());
        perm.setSummoningSick(false);
        return perm;
    }
}

