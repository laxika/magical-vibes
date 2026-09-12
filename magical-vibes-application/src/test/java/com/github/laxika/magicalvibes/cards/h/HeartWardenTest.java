package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(HeartWarden.class)
class HeartWardenTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Heart Warden produces one green mana")
    void tappingProducesGreenMana() {
        Permanent warden = addCreatureReady(player1, new HeartWarden());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(warden.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A summoning-sick Heart Warden cannot tap for mana")
    void summoningSickCannotTapForMana() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new HeartWarden());
        warden.setSummoningSick(true);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(warden.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Sacrificing Heart Warden draws a card")
    void sacrificeDrawsCard() {
        addCreatureReady(player1, new HeartWarden());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Heart Warden");
        harness.assertInGraveyard(player1, "Heart Warden");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Sacrifice ability pays its mana and sacrifice costs before drawing")
    void sacrificeCostsArePaidBeforeResolution() {
        Permanent warden = addCreatureReady(player1, new HeartWarden());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(warden);
        harness.assertInGraveyard(player1, "Heart Warden");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Cannot activate the draw ability without two mana")
    void sacrificeAbilityRequiresTwoMana() {
        Permanent warden = addCreatureReady(player1, new HeartWarden());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(warden);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }
}
