package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.ElvishScout;
import com.github.laxika.magicalvibes.cards.f.FungalBloom;
import com.github.laxika.magicalvibes.cards.r.RiverMerfolk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HomaridShaman.class, ElvishScout.class, RiverMerfolk.class, FungalBloom.class})
class HomaridShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Taps target green creature")
    void tapsTargetGreenCreature() {
        harness.addToBattlefield(player1, new HomaridShaman());
        Permanent scout = harness.addToBattlefieldAndReturn(player2, new ElvishScout());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, scout.getId());
        harness.passBothPriorities();

        assertThat(scout.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating the ability does not tap Homarid Shaman")
    void activatingAbilityDoesNotTapSource() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new HomaridShaman());
        Permanent scout = harness.addToBattlefieldAndReturn(player2, new ElvishScout());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, scout.getId());
        harness.passBothPriorities();

        assertThat(shaman.isTapped()).isFalse();
        assertThat(scout.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-green creature")
    void cannotTargetNonGreenCreature() {
        harness.addToBattlefield(player1, new HomaridShaman());
        Permanent merfolk = harness.addToBattlefieldAndReturn(player2, new RiverMerfolk());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, merfolk.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(merfolk.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a green noncreature permanent")
    void cannotTargetGreenNoncreaturePermanent() {
        harness.addToBattlefield(player1, new HomaridShaman());
        Permanent bloom = harness.addToBattlefieldAndReturn(player2, new FungalBloom());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bloom.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bloom.isTapped()).isFalse();
    }
}
