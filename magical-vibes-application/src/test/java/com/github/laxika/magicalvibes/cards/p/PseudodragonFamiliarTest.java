package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.SteadfastPaladin;
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

@CardUsed({PseudodragonFamiliar.class, SteadfastPaladin.class})
class PseudodragonFamiliarTest extends BaseCardTest {

    @Test
    @DisplayName("Ability grants flying to target creature")
    void grantsFlying() {
        harness.addToBattlefieldAndReturn(player1, new PseudodragonFamiliar());
        Permanent target = addCreature(player1);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Ability can target a creature an opponent controls")
    void grantsFlyingToOpponentCreature() {
        harness.addToBattlefieldAndReturn(player1, new PseudodragonFamiliar());
        Permanent target = addCreature(player2);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Granted flying wears off at end of turn")
    void flyingWearsOff() {
        harness.addToBattlefieldAndReturn(player1, new PseudodragonFamiliar());
        Permanent target = addCreature(player1);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(target.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefieldAndReturn(player1, new PseudodragonFamiliar());
        Permanent target = addCreature(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability can be activated while tapped and summoning sick")
    void activatesWhileTappedAndSummoningSick() {
        Permanent familiar = harness.addToBattlefieldAndReturn(player1, new PseudodragonFamiliar());
        familiar.setTapped(true);
        familiar.setSummoningSick(true);
        Permanent target = addCreature(player1);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(target.hasKeyword(Keyword.FLYING)).isFalse();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(familiar.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability still resolves after its source leaves the battlefield")
    void resolvesAfterSourceLeaves() {
        Permanent familiar = harness.addToBattlefieldAndReturn(player1, new PseudodragonFamiliar());
        Permanent target = addCreature(player1);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(familiar);
        gd.playerGraveyards.get(player1.getId()).add(familiar.getCard());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Generic mana cannot pay the blue portion of the activation cost")
    void requiresBlueMana() {
        harness.addToBattlefieldAndReturn(player1, new PseudodragonFamiliar());
        Permanent target = addCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private Permanent addCreature(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new SteadfastPaladin());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
