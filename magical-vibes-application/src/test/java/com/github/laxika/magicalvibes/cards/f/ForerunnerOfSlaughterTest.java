package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CoralhelmGuide;
import com.github.laxika.magicalvibes.cards.e.EldraziDevastator;
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

@CardUsed({ForerunnerOfSlaughter.class, EldraziDevastator.class, CoralhelmGuide.class})
class ForerunnerOfSlaughterTest extends BaseCardTest {

    @Test
    @DisplayName("Grants haste to a target colorless creature until end of turn")
    void grantsHasteUntilEndOfTurn() {
        Permanent forerunner = addReadyForerunner(player1);
        Permanent target = addCreatureReady(player2, new EldraziDevastator());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(forerunner.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a colored creature")
    void cannotTargetColoredCreature() {
        addReadyForerunner(player1);
        Permanent target = addCreatureReady(player2, new CoralhelmGuide());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a colorless creature");
    }

    @Test
    @DisplayName("Can target itself while summoning sick and pay generic mana with red mana")
    void canGrantItselfHasteWhileSummoningSick() {
        Permanent forerunner = harness.addToBattlefieldAndReturn(player1, new ForerunnerOfSlaughter());
        forerunner.setSummoningSick(true);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, forerunner.getId());

        assertThat(forerunner.hasKeyword(Keyword.HASTE)).isFalse();
        harness.passBothPriorities();

        assertThat(forerunner.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(forerunner.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can activate while tapped and grants haste even if the source leaves before resolution")
    void resolvesAfterTappedSourceLeavesBattlefield() {
        Permanent forerunner = addReadyForerunner(player1);
        forerunner.setTapped(true);
        Permanent target = addCreatureReady(player1, new EldraziDevastator());
        target.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(forerunner);
        gd.playerGraveyards.get(player1.getId()).add(forerunner.getCard());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant haste when the target leaves before resolution")
    void doesNotGrantHasteToDepartedTarget() {
        addReadyForerunner(player1);
        Permanent target = addCreatureReady(player2, new EldraziDevastator());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyForerunner(Player player) {
        return addCreatureReady(player, new ForerunnerOfSlaughter());
    }
}
