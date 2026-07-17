package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RakeclawGargantuanTest extends BaseCardTest {

    @Test
    @DisplayName("Grants first strike to target creature with power 5 or greater")
    void grantsFirstStrikeToHighPowerCreature() {
        addCreatureReady(player1, new RakeclawGargantuan());
        Permanent avatar = addCreatureReady(player1, new AvatarOfMight()); // 8/8
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, avatar.getId());
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, avatar.getId()).getGrantedKeywords())
                .contains(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("First strike wears off at end of turn")
    void firstStrikeWearsOff() {
        addCreatureReady(player1, new RakeclawGargantuan());
        Permanent avatar = addCreatureReady(player1, new AvatarOfMight());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, avatar.getId());
        harness.passBothPriorities();
        assertThat(gqs.findPermanentById(gd, avatar.getId()).getGrantedKeywords())
                .contains(Keyword.FIRST_STRIKE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, avatar.getId()).getGrantedKeywords())
                .doesNotContain(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Cannot target a creature with power less than 5")
    void cannotTargetLowPowerCreature() {
        addCreatureReady(player1, new RakeclawGargantuan());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears()); // 2/2
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
