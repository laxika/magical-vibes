package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArdentSoldier;
import com.github.laxika.magicalvibes.cards.d.DreamThrush;
import com.github.laxika.magicalvibes.cards.d.Duskwalker;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KavuAggressor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MightWeaver.class, KavuAggressor.class, ArdentSoldier.class, Duskwalker.class,
        DreamThrush.class, Island.class})
class MightWeaverTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability puts it on the stack with target")
    void activatingPutsOnStackWithTarget() {
        addCreatureReady(player1, new MightWeaver());
        Permanent target = addCreatureReady(player1, new KavuAggressor());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Resolving ability grants trample to red creature")
    void resolvingGrantsTrampleToRedCreature() {
        addCreatureReady(player1, new MightWeaver());
        Permanent target = addCreatureReady(player1, new KavuAggressor());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Resolving ability grants trample to white creature")
    void resolvingGrantsTrampleToWhiteCreature() {
        addCreatureReady(player1, new MightWeaver());
        Permanent target = addCreatureReady(player1, new ArdentSoldier());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Can target opponent's white creature")
    void canTargetOpponentWhiteCreature() {
        addCreatureReady(player1, new MightWeaver());
        Permanent target = addCreatureReady(player2, new ArdentSoldier());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Trample is removed at end of turn")
    void trampleRemovedAtEndOfTurn() {
        addCreatureReady(player1, new MightWeaver());
        Permanent target = addCreatureReady(player1, new KavuAggressor());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target black creature")
    void cannotTargetBlackCreature() {
        addCreatureReady(player1, new MightWeaver());
        Permanent target = addCreatureReady(player1, new Duskwalker());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a");
    }

    @Test
    @DisplayName("Cannot target blue creature")
    void cannotTargetBlueCreature() {
        addCreatureReady(player1, new MightWeaver());
        Permanent target = addCreatureReady(player1, new DreamThrush());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new MightWeaver());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a");
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new MightWeaver());
        Permanent target = addCreatureReady(player1, new KavuAggressor());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
