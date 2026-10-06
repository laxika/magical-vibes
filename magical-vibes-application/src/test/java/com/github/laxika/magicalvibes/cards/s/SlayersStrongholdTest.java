package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MoorlandInquisitor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlayersStronghold.class, MoorlandInquisitor.class})
class SlayersStrongholdTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: adds {C} without using the stack")
    void tapsForColorless() {
        Permanent stronghold = harness.addToBattlefieldAndReturn(player1, new SlayersStronghold());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(stronghold.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("{R}{W}, {T}: target creature gets +2/+0 and gains vigilance and haste")
    void pumpsAndGrantsKeywords() {
        harness.addToBattlefieldAndReturn(player1, new SlayersStronghold());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The pump ability can target an opponent's creature")
    void canTargetOpponentCreature() {
        harness.addToBattlefieldAndReturn(player1, new SlayersStronghold());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new MoorlandInquisitor());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Boost and keywords wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.addToBattlefieldAndReturn(player1, new SlayersStronghold());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The pump ability cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefieldAndReturn(player1, new SlayersStronghold());
        Permanent otherLand = harness.addToBattlefieldAndReturn(player1, new SlayersStronghold());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, otherLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Pump activation pays both colored mana and taps the land before resolution")
    void paysCostsBeforeResolution() {
        Permanent stronghold = harness.addToBattlefieldAndReturn(player1, new SlayersStronghold());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, creature.getId());

        assertThat(stronghold.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Colorless mana cannot replace the white activation cost")
    void cannotPayWithWrongColors() {
        Permanent stronghold = harness.addToBattlefieldAndReturn(player1, new SlayersStronghold());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(stronghold.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Stronghold cannot activate either ability")
    void cannotActivateWhileTapped() {
        Permanent stronghold = harness.addToBattlefieldAndReturn(player1, new SlayersStronghold());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        stronghold.tap();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The activated ability resolves after its source leaves the battlefield")
    void resolvesWithoutSource() {
        Permanent stronghold = harness.addToBattlefieldAndReturn(player1, new SlayersStronghold());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, 1, null, creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(stronghold);
        gd.playerGraveyards.get(player1.getId()).add(stronghold.getCard());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("An ability whose target leaves does not affect a replacement creature")
    void doesNotAffectReplacementCreature() {
        harness.addToBattlefieldAndReturn(player1, new SlayersStronghold());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, 1, null, creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        Permanent replacement = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, replacement)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, replacement, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, replacement, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Two Strongholds give cumulative power bonuses")
    void multipleActivationsStack() {
        harness.addToBattlefieldAndReturn(player1, new SlayersStronghold());
        harness.addToBattlefieldAndReturn(player1, new SlayersStronghold());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.activateAbility(player1, 1, 1, null, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Granted haste allows a new creature to attack without tapping from vigilance")
    void newCreatureAttacksWithVigilance() {
        harness.addToBattlefieldAndReturn(player1, new SlayersStronghold());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        creature.setSummoningSick(true);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThat(als.canAttack(gd, creature, player1.getId())).isFalse();
        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(als.canAttack(gd, creature, player1.getId())).isTrue();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        gs.declareAttackers(gd, player1, List.of(1));

        assertThat(creature.isAttacking()).isTrue();
        assertThat(creature.isTapped()).isFalse();
    }
}
