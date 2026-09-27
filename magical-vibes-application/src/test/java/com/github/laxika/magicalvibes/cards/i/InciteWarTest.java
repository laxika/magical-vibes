package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.g.GreatFurnace;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InciteWar.class, AlphaMyr.class, GreatFurnace.class})
class InciteWarTest extends BaseCardTest {

    @Test
    @DisplayName("Attack mode requires all creatures of the target player to attack if able")
    void attackModeForcesTargetPlayersCreaturesToAttack() {
        Permanent first = addCreatureReady(player2, new AlphaMyr());
        Permanent second = addCreatureReady(player2, new AlphaMyr());

        cast(new int[]{0}, List.of(player2.getId()), false);

        int firstIndex = gd.playerBattlefields.get(player2.getId()).indexOf(first);
        assertThatThrownBy(() -> declareAttackers(player2, List.of(firstIndex)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(second.isAttacking()).isFalse();

        int secondIndex = gd.playerBattlefields.get(player2.getId()).indexOf(second);
        declareAttackers(player2, List.of(firstIndex, secondIndex));

        assertThat(first.isAttackedThisTurn()).isTrue();
        assertThat(second.isAttackedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Attack mode can target the caster's creatures")
    void attackModeCanTargetController() {
        Permanent ownCreature = addCreatureReady(player1, new AlphaMyr());

        cast(new int[]{0}, List.of(player1.getId()), false);

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ownCreature.isMustAttackThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Attack mode does not require a creature that cannot attack")
    void attackModeIgnoresCreatureUnableToAttack() {
        Permanent tappedCreature = addCreatureReady(player2, new AlphaMyr());
        tappedCreature.tap();

        cast(new int[]{0}, List.of(player2.getId()), false);

        declareAttackers(player2, List.of());

        assertThat(tappedCreature.isAttacking()).isFalse();
        assertThat(tappedCreature.isMustAttackThisTurn()).isTrue();
    }

    @Test
    @DisplayName("First-strike mode affects creatures you control only until end of turn")
    void firstStrikeMode() {
        Permanent ownCreature = addCreatureReady(player1, new AlphaMyr());
        Permanent opponentCreature = addCreatureReady(player2, new AlphaMyr());

        cast(new int[]{1}, List.of(), false);

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FIRST_STRIKE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("First-strike mode affects creatures, not other permanents")
    void firstStrikeModeExcludesNoncreatures() {
        Permanent ownCreature = addCreatureReady(player1, new AlphaMyr());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new GreatFurnace());

        cast(new int[]{1}, List.of(), false);

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownLand, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Entwine pays {2} and resolves both modes")
    void entwineResolvesBothModes() {
        Permanent ownCreature = addCreatureReady(player1, new AlphaMyr());
        Permanent targetCreature = addCreatureReady(player2, new AlphaMyr());

        cast(new int[]{0, 1}, List.of(player2.getId()), true);

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FIRST_STRIKE)).isTrue();
        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(targetCreature.isAttacking()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Entwine is rejected without its additional mana")
    void entwineRequiresAdditionalMana() {
        harness.setHand(player1, List.of(new InciteWar()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Attack mode rejects a non-player target")
    void attackModeRejectsNonPlayerTarget() {
        Permanent creature = addCreatureReady(player2, new AlphaMyr());
        harness.setHand(player1, List.of(new InciteWar()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, List<UUID> targetIds, boolean entwined) {
        harness.setHand(player1, List.of(new InciteWar()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, entwined ? 4 : 2);
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, targetIds);
        harness.passBothPriorities();
    }
}
