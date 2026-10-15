package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MotherBear;
import com.github.laxika.magicalvibes.cards.n.NimbleMongoose;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StirringAddress.class, MotherBear.class, NimbleMongoose.class})
class StirringAddressTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature you control gets +2/+2")
    void pumpsTargetCreature() {
        Permanent target = addCreature(player1);
        Permanent enemy = addCreature(player2);
        harness.setHand(player1, List.of(new StirringAddress()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, enemy)).isEqualTo(2);
    }

    @Test
    @DisplayName("The +2/+2 wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent target = addCreature(player1);
        harness.setHand(player1, List.of(new StirringAddress()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        addCreature(player1);
        Permanent enemy = addCreature(player2);
        harness.setHand(player1, List.of(new StirringAddress()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, enemy.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Overloaded, every creature you control gets +2/+2 and no target is chosen")
    void overloadPumpsEveryCreatureYouControl() {
        Permanent first = addCreature(player1);
        Permanent second = addCreature(player1);
        Permanent enemy = addCreature(player2);
        harness.setHand(player1, List.of(new StirringAddress()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, enemy)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enemy)).isEqualTo(2);
    }

    @Test
    @DisplayName("Overload cannot be paid with only the normal mana cost available")
    void overloadRequiresTheFullOverloadCost() {
        addCreature(player1);
        harness.setHand(player1, List.of(new StirringAddress()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithOverload(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A target that changes controller before resolution is not boosted")
    void targetMustStillBeControlledAtResolution() {
        Permanent target = addCreature(player1);
        harness.setHand(player1, List.of(new StirringAddress()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Stirring Address");
    }

    @Test
    @DisplayName("Overload can be cast without controlling any creatures")
    void overloadDoesNotRequireAnyCreatures() {
        Permanent enemy = addCreature(player2);
        harness.setHand(player1, List.of(new StirringAddress()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enemy)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enemy)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Stirring Address");
    }

    @Test
    @DisplayName("Overload affects creatures present at resolution, but not creatures entering later")
    void overloadDeterminesAffectedCreaturesAtResolution() {
        Permanent original = addCreature(player1);
        harness.setHand(player1, List.of(new StirringAddress()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castWithOverload(player1, 0);
        Permanent beforeResolution = harness.enterBattlefieldAndReturn(player1, new MotherBear());
        harness.passBothPriorities();
        Permanent afterResolution = harness.enterBattlefieldAndReturn(player1, new MotherBear());

        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, beforeResolution)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, afterResolution)).isEqualTo(2);
    }

    @Test
    @DisplayName("The overloaded boost wears off for every creature at end of turn")
    void overloadedBoostWearsOffAtEndOfTurn() {
        Permanent first = addCreature(player1);
        Permanent second = addCreature(player1);
        harness.setHand(player1, List.of(new StirringAddress()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }

    @Test
    @DisplayName("Normal casting boosts only the chosen creature you control")
    void normalCastingDoesNotBoostOtherOwnCreatures() {
        Permanent target = addCreature(player1);
        Permanent other = addCreature(player1);
        harness.setHand(player1, List.of(new StirringAddress()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("Normal casting cannot target a creature with shroud")
    void normalCastingCannotTargetShroud() {
        Permanent mongoose = harness.addToBattlefieldAndReturn(player1, new NimbleMongoose());
        harness.setHand(player1, List.of(new StirringAddress()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, mongoose.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Overload boosts a creature with shroud because it does not target")
    void overloadBoostsShroudCreature() {
        Permanent mongoose = harness.addToBattlefieldAndReturn(player1, new NimbleMongoose());
        harness.setHand(player1, List.of(new StirringAddress()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mongoose)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mongoose)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Stirring Address");
    }

    private Permanent addCreature(Player player) {
        return addCreatureReady(player, new MotherBear());
    }
}
