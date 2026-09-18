package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FangrenHunter;
import com.github.laxika.magicalvibes.cards.h.HumOfTheRadix;
import com.github.laxika.magicalvibes.cards.m.MyrRetriever;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrollsOfTelJilad.class, FangrenHunter.class, MyrRetriever.class, HumOfTheRadix.class})
class TrollsOfTelJiladTest extends BaseCardTest {

    @Test
    @DisplayName("Regenerates a target green creature")
    void regeneratesTargetGreenCreature() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new TrollsOfTelJilad());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FangrenHunter());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        assertThat(source.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();

        assertThat(target.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can regenerate an opponent's green creature")
    void regeneratesOpponentsGreenCreature() {
        harness.addToBattlefield(player1, new TrollsOfTelJilad());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FangrenHunter());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a nongreen creature")
    void cannotTargetNongreenCreature() {
        harness.addToBattlefield(player1, new TrollsOfTelJilad());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MyrRetriever());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a green creature");
    }

    @Test
    @DisplayName("Cannot target a green noncreature permanent")
    void cannotTargetGreenNoncreaturePermanent() {
        harness.addToBattlefield(player1, new TrollsOfTelJilad());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HumOfTheRadix());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a green creature");
    }
}
