package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RakdosShredFreak.class})
class RakdosShredFreakTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack immediately due to haste")
    void canAttackImmediatelyDueToHaste() {
        harness.setLife(player2, 20);

        Permanent shredFreak = harness.addToBattlefieldAndReturn(player1, new RakdosShredFreak());
        shredFreak.setSummoningSick(true);

        declareAttackers(List.of(0));

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Haste does not allow a tapped creature to attack")
    void cannotAttackWhileTapped() {
        Permanent shredFreak = harness.addToBattlefieldAndReturn(player1, new RakdosShredFreak());
        shredFreak.setSummoningSick(true);

        assertThat(als.canAttack(gd, shredFreak, player1.getId())).isTrue();

        shredFreak.setTapped(true);

        assertThat(als.canAttack(gd, shredFreak, player1.getId())).isFalse();
    }
}
