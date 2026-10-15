package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MegaFlare.class, AirElemental.class, AvatarOfMight.class})
class MegaFlareTest extends BaseCardTest {

    @Test
    void dealsDamageEqualToGreatestControlledPower() {
        harness.addToBattlefield(player1, new AirElemental());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        harness.setHand(player1, List.of(new MegaFlare()));
        addMana(1);

        harness.castSorcery(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void kickedSpellCreatesDragon() {
        harness.setHand(player1, List.of(new MegaFlare()));
        addMana(2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Dragon")).isEqualTo(1);
    }

    @Test
    void allowsAtMostOneTargetPerOpponent() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        harness.setHand(player1, List.of(new MegaFlare()));
        addMana(1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one permanent per controller");
    }

    private void addMana(int red) {
        harness.addMana(player1, ManaColor.RED, red);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
