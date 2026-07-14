package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.SteadfastGuard;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AntlerSkulkinTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability grants persist to a white creature")
    void resolvingGrantsPersistToWhiteCreature() {
        addReadySkulkin(player1);
        Permanent target = addReadyWhiteCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.PERSIST)).isTrue();
    }

    @Test
    @DisplayName("Can target an opponent's white creature")
    void canTargetOpponentWhiteCreature() {
        addReadySkulkin(player1);
        Permanent target = addReadyWhiteCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.PERSIST)).isTrue();
    }

    @Test
    @DisplayName("Persist is removed at end of turn")
    void persistRemovedAtEndOfTurn() {
        addReadySkulkin(player1);
        Permanent target = addReadyWhiteCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.hasKeyword(Keyword.PERSIST)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.PERSIST)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-white creature")
    void cannotTargetNonWhiteCreature() {
        addReadySkulkin(player1);
        Permanent target = addReadyRedCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a white creature");
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadySkulkin(player1);
        Permanent target = addReadyWhiteCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addReadySkulkin(Player player) {
        return addReady(player, new AntlerSkulkin());
    }

    private Permanent addReadyWhiteCreature(Player player) {
        return addReady(player, new SteadfastGuard());
    }

    private Permanent addReadyRedCreature(Player player) {
        return addReady(player, new RagingGoblin());
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
