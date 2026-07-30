package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
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

class CrimsonMageTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability grants haste to a creature you control")
    void resolvingGrantsHaste() {
        addReadyMage(player1);
        Permanent target = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Haste is removed at end of turn")
    void hasteRemovedAtEndOfTurn() {
        addReadyMage(player1);
        Permanent target = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        addReadyMage(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Ability does not require tapping the source")
    void abilityCanBeActivatedTwiceWithoutTapping() {
        addReadyMage(player1);
        Permanent first = addReadyCreature(player1);
        Permanent second = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(first.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(second.hasKeyword(Keyword.HASTE)).isTrue();
    }

    private Permanent addReadyMage(Player player) {
        Permanent perm = new Permanent(new CrimsonMage());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new FugitiveWizard());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
