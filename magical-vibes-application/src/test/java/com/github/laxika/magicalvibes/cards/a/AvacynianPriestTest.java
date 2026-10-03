package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.d.DoomedTraveler;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.b.BlazingTorch;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvacynianPriest.class, WalkingCorpse.class, DoomedTraveler.class, BlazingTorch.class})
class AvacynianPriestTest extends BaseCardTest {


    @Test
    @DisplayName("Resolving ability taps target non-Human creature")
    void resolvingTapsNonHumanCreature() {
        addCreatureReady(player1, new AvacynianPriest());
        Permanent target = addCreatureReady(player2, new WalkingCorpse());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingPutsOnStack() {
        addCreatureReady(player1, new AvacynianPriest());
        Permanent target = addCreatureReady(player2, new WalkingCorpse());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Activating ability taps the priest")
    void activatingTapsPriest() {
        Permanent priest = addCreatureReady(player1, new AvacynianPriest());
        Permanent target = addCreatureReady(player2, new WalkingCorpse());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(priest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can tap own non-Human creature")
    void canTapOwnNonHumanCreature() {
        addCreatureReady(player1, new AvacynianPriest());
        Permanent ownBears = addCreatureReady(player1, new WalkingCorpse());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, ownBears.getId());
        harness.passBothPriorities();

        assertThat(ownBears.isTapped()).isTrue();
    }


    @Test
    @DisplayName("Cannot target a Human creature")
    void cannotTargetHumanCreature() {
        addCreatureReady(player1, new AvacynianPriest());
        Permanent human = addCreatureReady(player2, new DoomedTraveler());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, human.getId()))
                .isInstanceOf(IllegalStateException.class);
    }


    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new AvacynianPriest());
        Permanent target = addCreatureReady(player2, new WalkingCorpse());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }


    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addCreatureReady(player1, new AvacynianPriest());
        Permanent target = addCreatureReady(player2, new WalkingCorpse());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }


    @Test
    @DisplayName("Cannot target a noncreature artifact")
    void cannotTargetNoncreatureArtifact() {
        addCreatureReady(player1, new AvacynianPriest());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BlazingTorch());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate a tapped priest")
    void cannotActivateTappedPriest() {
        Permanent priest = addCreatureReady(player1, new AvacynianPriest());
        priest.setTapped(true);
        Permanent target = addCreatureReady(player2, new WalkingCorpse());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate a priest with summoning sickness")
    void cannotActivateSummoningSickPriest() {
        Permanent priest = addCreatureReady(player1, new AvacynianPriest());
        priest.setSummoningSick(true);
        Permanent target = addCreatureReady(player2, new WalkingCorpse());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(priest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An already tapped non-Human creature is a legal target")
    void canTargetTappedCreature() {
        addCreatureReady(player1, new AvacynianPriest());
        Permanent target = addCreatureReady(player2, new WalkingCorpse());
        target.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("The ability resolves after the priest leaves the battlefield")
    void resolvesAfterPriestLeavesBattlefield() {
        Permanent priest = addCreatureReady(player1, new AvacynianPriest());
        Permanent target = addCreatureReady(player2, new WalkingCorpse());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(priest);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Generic activation mana may be paid with colored mana")
    void canPayWithColoredMana() {
        addCreatureReady(player1, new AvacynianPriest());
        Permanent target = addCreatureReady(player2, new WalkingCorpse());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }
}
