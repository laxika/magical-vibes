package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ManalithTest extends BaseCardTest {

    @Test
    @DisplayName("Mana ability adds one mana of the chosen color and taps Manalith")
    void manaAbilityAddsChosenColor() {
        Permanent manalith = harness.addToBattlefieldAndReturn(player1, new Manalith());
        manalith.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(manalith.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
