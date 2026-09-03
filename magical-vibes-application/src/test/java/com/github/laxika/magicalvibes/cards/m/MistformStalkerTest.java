package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(MistformStalker.class)
class MistformStalkerTest extends BaseCardTest {

    @Test
    void chosenCreatureTypeReplacesOldTypeUntilEndOfTurn() {
        Permanent stalker = addReadyStalker();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        assertThat(gqs.effectiveCreatureSubtypes(gd, stalker)).containsExactly(CardSubtype.GOBLIN);
    }

    @Test
    void secondAbilityBoostsAndGrantsFlying() {
        Permanent stalker = addReadyStalker();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, stalker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, stalker)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, stalker, Keyword.FLYING)).isTrue();
    }

    @Test
    void secondAbilityWearsOffAtEndOfTurn() {
        Permanent stalker = addReadyStalker();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, stalker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, stalker)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, stalker, Keyword.FLYING)).isFalse();
    }

    private Permanent addReadyStalker() {
        Permanent stalker = new Permanent(new MistformStalker());
        stalker.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(stalker);
        return stalker;
    }
}
