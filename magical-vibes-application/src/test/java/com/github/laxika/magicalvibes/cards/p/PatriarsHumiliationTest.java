package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanHellkite;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PatriarsHumiliation.class, GrizzlyBears.class, ShivanHellkite.class})
class PatriarsHumiliationTest extends BaseCardTest {

    @Test
    @DisplayName("Perpetually removes abilities and deals damage equal to your creatures")
    void removesAbilitiesAndDealsDamageEqualToControlledCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ShivanHellkite());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        castHumiliation(target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();

        harness.addMana(player2, ManaColor.RED, 2);
        assertThatThrownBy(() -> harness.activateAbility(player2, battlefieldIndex(player2, target), 0,
                null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target a creature an opponent controls")
    void canTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castHumiliation(target);

        assertThat(target.getMarkedDamage()).isEqualTo(0);
    }

    private void castHumiliation(Permanent target) {
        harness.setHand(player1, List.of(new PatriarsHumiliation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
