package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GoblinBrigand;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PemminsAura.class, GoblinBrigand.class})
class PemminsAuraTest extends BaseCardTest {

    @Test
    void castsAndAttachesToTargetCreature() {
        Permanent creature = addCreatureReady(player1, new GoblinBrigand());
        harness.setHand(player1, List.of(new PemminsAura()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Pemmin's Aura").getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void untapsEnchantedCreature() {
        Permanent creature = attachAuraToReadyCreature();
        creature.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        activate(0);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void grantsFlyingAndShroudUntilEndOfTurn() {
        Permanent creature = attachAuraToReadyCreature();
        harness.addMana(player1, ManaColor.BLUE, 2);

        activate(1);
        activate(2);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isFalse();
    }

    @Test
    void shroudPreventsTargetingTheEnchantedCreature() {
        Permanent creature = attachAuraToReadyCreature();
        harness.addMana(player1, ManaColor.BLUE, 1);

        activate(2);

        harness.setHand(player1, List.of(new PemminsAura()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void choosesEitherPowerToughnessAdjustment() {
        Permanent creature = attachAuraToReadyCreature();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activate(3);
        harness.handleListChoice(player1, "Enchanted creature gets +1/-1 until end of turn");

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    void choosesTheOtherPowerToughnessAdjustment() {
        Permanent creature = attachAuraToReadyCreature();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activate(3);
        harness.handleListChoice(player1, "Enchanted creature gets -1/+1 until end of turn");

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void powerToughnessAdjustmentExpiresAtEndOfTurn() {
        Permanent creature = attachAuraToReadyCreature();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activate(3);
        harness.handleListChoice(player1, "Enchanted creature gets +1/-1 until end of turn");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    private void activate(int abilityIndex) {
        int auraIndex = gd.playerBattlefields.get(player1.getId()).size() - 1;
        harness.activateAbility(player1, auraIndex, abilityIndex, null, null);
        harness.passBothPriorities();
    }

    private Permanent attachAuraToReadyCreature() {
        Permanent creature = addCreatureReady(player1, new GoblinBrigand());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PemminsAura());
        aura.setAttachedTo(creature.getId());
        return creature;
    }
}
