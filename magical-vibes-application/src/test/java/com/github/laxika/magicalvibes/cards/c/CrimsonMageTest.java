package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrimsonMage.class, GoblinPiker.class})
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

    @Test
    @DisplayName("A summoning-sick Crimson Mage can target itself")
    void summoningSickMageCanTargetItself() {
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new CrimsonMage());
        mage.setSummoningSick(true);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, mage.getId());

        assertThat(mage.hasKeyword(Keyword.HASTE)).isFalse();
        harness.passBothPriorities();

        assertThat(mage.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(mage.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped Crimson Mage can activate its ability")
    void tappedMageCanActivate() {
        Permanent mage = addReadyMage(player1);
        mage.tap();
        Permanent target = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(mage.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability resolves after Crimson Mage leaves the battlefield")
    void abilityResolvesWithoutSource() {
        Permanent mage = addReadyMage(player1);
        Permanent target = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(mage);
        gd.playerGraveyards.get(player1.getId()).add(mage.getCard());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Target must still be controlled by the ability controller at resolution")
    void targetChangingControllerDoesNotGainHaste() {
        addReadyMage(player1);
        Permanent target = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability requires red mana")
    void cannotActivateWithoutRedMana() {
        addReadyMage(player1);
        Permanent target = addReadyCreature(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyMage(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new CrimsonMage());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new GoblinPiker());
        perm.setSummoningSick(false);
        return perm;
    }
}
