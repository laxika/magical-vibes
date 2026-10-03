package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({FrenziedFugue.class, GrizzlyBears.class})
class FrenziedFugueTest extends BaseCardTest {

    @Test
    @DisplayName("Entering gains control of, untaps, and hastes the enchanted permanent")
    void entersGainsControlUntapsAndHastes() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();

        castFrenziedFugue(target);

        assertThat(controls(player1, target)).isTrue();
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
    }

    @Test
    @DisplayName("Control and haste revert at cleanup")
    void controlAndHasteExpireAtCleanup() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castFrenziedFugue(target);
        endTheTurn();

        assertThat(controls(player2, target)).isTrue();
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
    }

    @Test
    @DisplayName("Your upkeep repeats the control, untap, and haste effect")
    void yourUpkeepRepeatsEffect() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FrenziedFugue());
        aura.setAttachedTo(target.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(controls(player1, target)).isTrue();
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The upkeep trigger does not fire during the enchanted permanent's controller's upkeep")
    void upkeepTriggerUsesAuraController() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FrenziedFugue());
        aura.setAttachedTo(target.getId());

        advanceToUpkeep(player2);
        target.tap();
        harness.passBothPriorities();

        assertThat(controls(player2, target)).isTrue();
        assertThat(target.isTapped()).isTrue();
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    private void castFrenziedFugue(Permanent target) {
        harness.setHand(player1, List.of(new FrenziedFugue()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void endTheTurn() {
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private boolean controls(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .anyMatch(p -> p.getId().equals(permanent.getId()));
    }
}
