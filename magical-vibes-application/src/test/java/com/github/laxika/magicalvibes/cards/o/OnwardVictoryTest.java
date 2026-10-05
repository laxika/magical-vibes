package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SupernaturalStamina;
import com.github.laxika.magicalvibes.cards.t.ThoseWhoServe;
import com.github.laxika.magicalvibes.cards.w.WindsOfRebuke;
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

@CardUsed({OnwardVictory.class, ThoseWhoServe.class, Forest.class, SupernaturalStamina.class, WindsOfRebuke.class})
class OnwardVictoryTest extends BaseCardTest {

    @Test
    @DisplayName("Onward gives +X/+0 equal to the target's power until end of turn")
    void onwardPumpsByOwnPower() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ThoseWhoServe());
        harness.setHand(player1, List.of(new OnwardVictory()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        // Those Who Serve gets +2/+0, becoming 4/4.
        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
        assertThat(target.getEffectivePower()).isEqualTo(4);
        harness.assertInGraveyard(player1, "Onward");
    }

    @Test
    @DisplayName("Onward boost wears off at end of turn")
    void onwardBoostWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ThoseWhoServe());
        harness.setHand(player1, List.of(new OnwardVictory()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Onward cannot target a non-creature")
    void onwardCannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new OnwardVictory()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Victory from graveyard grants double strike, then exiles")
    void victoryGrantsDoubleStrikeAndExiles() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ThoseWhoServe());
        harness.setGraveyard(player1, List.of(new OnwardVictory()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveFlashback(player1, 0, target.getId());

        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Onward") || c.getName().equals("Victory"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Onward"));
    }

    @Test
    @DisplayName("Victory double strike wears off at end of turn")
    void victoryDoubleStrikeWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ThoseWhoServe());
        harness.setGraveyard(player1, List.of(new OnwardVictory()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveFlashback(player1, 0, target.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Victory cannot target a non-creature")
    void victoryCannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setGraveyard(player1, List.of(new OnwardVictory()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Victory requires sorcery timing")
    void victoryRequiresSorceryTiming() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ThoseWhoServe());
        harness.setGraveyard(player1, List.of(new OnwardVictory()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");
    }

    @Test
    @DisplayName("Onward reads power at resolution and its boost stays fixed afterward")
    void onwardUsesResolutionPowerAndDoesNotRecalculate() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ThoseWhoServe());
        harness.setHand(player1, List.of(new OnwardVictory(), new SupernaturalStamina(), new SupernaturalStamina()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(8);

        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("Onward gives no boost to a creature with negative power")
    void onwardDoesNotFurtherReduceNegativePower() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ThoseWhoServe());
        target.setPowerModifier(-3);
        harness.setHand(player1, List.of(new OnwardVictory()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(-1);
        assertThat(target.getPowerModifier()).isEqualTo(-3);
        harness.assertInGraveyard(player1, "Onward");
    }

    @Test
    @DisplayName("Onward gives no boost to a creature with zero power")
    void onwardHandlesZeroPower() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ThoseWhoServe());
        target.setPowerModifier(-2);
        harness.setHand(player1, List.of(new OnwardVictory()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isZero();
        assertThat(target.getPowerModifier()).isEqualTo(-2);
        harness.assertInGraveyard(player1, "Onward");
    }

    @Test
    @DisplayName("Both halves can target an opponent's creature using the same physical card")
    void bothHalvesCanTargetOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ThoseWhoServe());
        OnwardVictory card = new OnwardVictory();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);

        harness.castAndResolveFlashback(player1, 0, target.getId());
        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    @Test
    @DisplayName("Victory is exiled even when its target leaves before resolution")
    void victoryExilesWhenTargetBecomesIllegal() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ThoseWhoServe());
        OnwardVictory card = new OnwardVictory();
        harness.setGraveyard(player1, List.of(card));
        harness.setHand(player1, List.of(new WindsOfRebuke()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.assertInHand(player1, "Those Who Serve");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    @Test
    @DisplayName("Onward goes to the graveyard when its target leaves before resolution")
    void onwardGoesToGraveyardWhenTargetBecomesIllegal() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ThoseWhoServe());
        OnwardVictory card = new OnwardVictory();
        harness.setHand(player1, List.of(card, new WindsOfRebuke()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Those Who Serve");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("Victory cannot be cast while another spell is on the stack")
    void victoryRequiresAnEmptyStack() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ThoseWhoServe());
        harness.setHand(player1, List.of(new OnwardVictory()));
        OnwardVictory aftermathCard = new OnwardVictory();
        harness.setGraveyard(player1, List.of(aftermathCard));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0, target.getId());

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(aftermathCard);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Victory cannot be cast during an opponent's main phase")
    void victoryRequiresItsControllersTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ThoseWhoServe());
        harness.setGraveyard(player1, List.of(new OnwardVictory()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");
        harness.assertInGraveyard(player1, "Onward");
        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isFalse();
    }
}
