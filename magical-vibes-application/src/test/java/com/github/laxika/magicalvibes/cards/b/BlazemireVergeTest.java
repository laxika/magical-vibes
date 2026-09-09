package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlazemireVerge.class, Swamp.class, Mountain.class})
class BlazemireVergeTest extends BaseCardTest {

    @Test
    void addsBlackManaWithoutRestriction() {
        Permanent verge = addReadyVerge();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(verge.isTapped()).isTrue();
    }

    @Test
    void redManaAbilityRequiresSwampOrMountain() {
        addReadyVerge();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void addsRedManaWhenControllingSwamp() {
        addReadyVerge();
        harness.addToBattlefield(player1, new Swamp());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void addsRedManaWhenControllingMountain() {
        addReadyVerge();
        harness.addToBattlefield(player1, new Mountain());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    private Permanent addReadyVerge() {
        Permanent verge = new Permanent(new BlazemireVerge());
        verge.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(verge);
        return verge;
    }
}
