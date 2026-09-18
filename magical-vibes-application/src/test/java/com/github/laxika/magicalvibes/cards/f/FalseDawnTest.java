package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BattlefieldForge;
import com.github.laxika.magicalvibes.cards.d.DegaDisciple;
import com.github.laxika.magicalvibes.cards.j.Jilt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FalseDawn.class, BattlefieldForge.class, DegaDisciple.class, Jilt.class})
class FalseDawnTest extends BaseCardTest {

    @Test
    @DisplayName("Turns colored mana from your abilities white and lets you spend it as any color")
    void replacesManaAndAllowsWhiteAsAnyColor() {
        harness.addToBattlefield(player1, new BattlefieldForge());
        harness.addToBattlefield(player1, new BattlefieldForge());
        var target = harness.addToBattlefieldAndReturn(player2, new DegaDisciple());
        harness.setHand(player1, List.of(new FalseDawn(), new Jilt()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Dega Disciple");
        harness.assertInHand(player2, "Dega Disciple");
    }

    @Test
    @DisplayName("Does not replace colorless mana")
    void leavesColorlessManaUnchanged() {
        harness.addToBattlefield(player1, new BattlefieldForge());
        harness.setHand(player1, List.of(new FalseDawn()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Replaces colored mana only from spells and abilities you control")
    void doesNotReplaceOpponentsColoredMana() {
        harness.addToBattlefield(player2, new BattlefieldForge());
        harness.setHand(player1, List.of(new FalseDawn()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.activateAbility(player2, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Only white mana gains the any-color spending permission")
    void doesNotMakeOtherManaAnyColor() {
        var target = harness.addToBattlefieldAndReturn(player2, new DegaDisciple());
        harness.setHand(player1, List.of(new FalseDawn(), new Jilt()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
