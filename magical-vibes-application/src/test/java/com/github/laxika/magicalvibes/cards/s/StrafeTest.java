package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AncientSpider;
import com.github.laxika.magicalvibes.cards.c.CloudCover;
import com.github.laxika.magicalvibes.cards.m.MoggJailer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Strafe.class, AncientSpider.class, MoggJailer.class, CloudCover.class})
class StrafeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to a nonred creature")
    void dealsDamageToNonredCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AncientSpider());
        harness.setHand(player1, List.of(new Strafe()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can target a nonred creature its controller controls")
    void canTargetOwnNonredCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AncientSpider());
        harness.setHand(player1, List.of(new Strafe()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a red creature")
    void cannotTargetRedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoggJailer());
        harness.setHand(player1, List.of(new Strafe()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonred creature");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CloudCover());
        harness.setHand(player1, List.of(new Strafe()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonred creature");
    }
}
