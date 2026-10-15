package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DaybreakCharger.class, WalkingCorpse.class})
class DaybreakChargerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives target creature +2/+0")
    void etbBoostsTargetCreature() {
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new DaybreakCharger()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player2, "Walking Corpse");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Walking Corpse");
        assertThat(bears.getPowerModifier()).isEqualTo(2);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new DaybreakCharger()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player2, "Walking Corpse");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Walking Corpse");
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB fizzles if target creature leaves before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new DaybreakCharger()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player2, "Walking Corpse");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Can be cast on an empty battlefield and target itself on entry")
    void castOnEmptyBattlefieldTargetsItself() {
        harness.setHand(player1, List.of(new DaybreakCharger()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Daybreak Charger");
        Permanent charger = findPermanent(player1, "Daybreak Charger");
        harness.handlePermanentChosen(player1, charger.getId());
        harness.passBothPriorities();

        assertThat(charger.getPowerModifier()).isEqualTo(2);
        assertThat(charger.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering without being cast triggers and can boost another creature you control")
    void enteringWithoutCastingBoostsFriendlyCreature() {
        Permanent corpse = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        harness.enterBattlefieldAndReturn(player1, new DaybreakCharger());
        harness.handlePermanentChosen(player1, corpse.getId());
        harness.passBothPriorities();

        assertThat(corpse.getPowerModifier()).isEqualTo(2);
        assertThat(corpse.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB still resolves after Daybreak Charger leaves the battlefield")
    void triggerResolvesWithoutSource() {
        Permanent corpse = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new DaybreakCharger()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0, corpse.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(corpse.getPowerModifier()).isEqualTo(2);
        assertThat(corpse.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
