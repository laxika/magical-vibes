package com.github.laxika.magicalvibes.cards.c;

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

@CardUsed({CradleOfSafety.class, CourierBat.class})
class CradleOfSafetyTest extends BaseCardTest {

    @Test
    @DisplayName("Gives the enchanted creature +1/+1 and hexproof until end of turn")
    void protectsAndBoostsEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CourierBat());
        castAndResolve(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Can target only a creature its controller controls")
    void cannotTargetOpponentCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CourierBat());
        harness.setHand(player1, List.of(new CradleOfSafety()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    void canBeCastDuringOpponentsTurnAndProtectsOnlyEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CourierBat());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new CourierBat());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.ensurePriority(player1);

        castAndResolve(creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    void entryTriggerResolvesAfterAuraLeavesBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CourierBat());
        harness.setHand(player1, List.of(new CradleOfSafety()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isFalse();
        harness.getPermanentRemovalService().removePermanentToHand(gd,
                findPermanent(player1, "Cradle of Safety"));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        harness.assertInHand(player1, "Cradle of Safety");
    }

    @Test
    void entryAbilityStillGrantsHexproofAfterCreatureChangesController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CourierBat());
        harness.setHand(player1, List.of(new CradleOfSafety()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.getPermanentRemovalService().removeOrphanedAuras(gd);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
        harness.assertInGraveyard(player1, "Cradle of Safety");
    }

    private void castAndResolve(Permanent target) {
        harness.setHand(player1, List.of(new CradleOfSafety()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, target.getId());
        resolveAllTriggers();
    }
}
