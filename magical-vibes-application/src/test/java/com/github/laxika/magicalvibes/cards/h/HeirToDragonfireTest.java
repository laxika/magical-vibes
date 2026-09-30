package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(HeirToDragonfire.class)
class HeirToDragonfireTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability gives +1/+0 until end of turn")
    void activatedAbilityBoostsUntilEndOfTurn() {
        Permanent heir = harness.addToBattlefieldAndReturn(player1, new HeirToDragonfire());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(heir.getEffectivePower()).isEqualTo(3);
        assertThat(heir.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(heir.getEffectivePower()).isEqualTo(2);
        assertThat(heir.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Hand ability keeps the card in hand and perpetually upgrades it")
    void handAbilityPerpetuallyUpgradesCard() {
        harness.setHand(player1, List.of(new HeirToDragonfire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Heir to Dragonfire");

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent heir = findPermanent(player1, "Heir to Dragonfire");
        assertThat(gqs.getEffectivePower(gd, heir)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, heir)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, heir, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, heir, CardSubtype.DRAGON)).isTrue();
    }
}
