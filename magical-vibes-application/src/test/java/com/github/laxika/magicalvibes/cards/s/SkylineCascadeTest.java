package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkylineCascade.class, GrizzlyBears.class})
class SkylineCascadeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and prevents a target opponent creature from untapping without tapping it")
    void entersTappedAndSkipsTargetCreatureUntap() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SkylineCascade()));

        gs.playCard(gd, player1, 0, 0, target.getId(), null);

        Permanent cascade = findPermanent(player1, "Skyline Cascade");
        assertThat(cascade.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping adds one blue mana")
    void tapsForBlueMana() {
        Permanent cascade = harness.addToBattlefieldAndReturn(player1, new SkylineCascade());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(cascade.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature its controller controls")
    void cannotTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SkylineCascade()));

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, target.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }
}
