package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RisenSanctuary.class})
class RisenSanctuaryTest extends BaseCardTest {

    @Test
    @DisplayName("Vigilance: attacking does not tap Risen Sanctuary")
    void vigilanceDoesNotTapWhenAttacking() {
        Permanent sanctuary = addCreatureReady(player1, new RisenSanctuary());

        declareAttackers(List.of(0));

        assertThat(sanctuary.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Vigilance does not allow a tapped creature to attack")
    void tappedSanctuaryCannotAttack() {
        Permanent sanctuary = addCreatureReady(player1, new RisenSanctuary());
        sanctuary.tap();

        assertThat(als.canAttack(gd, sanctuary, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("Vigilance does not bypass summoning sickness")
    void summoningSickSanctuaryCannotAttack() {
        Permanent sanctuary = harness.addToBattlefieldAndReturn(player1, new RisenSanctuary());

        assertThat(als.canAttack(gd, sanctuary, player1.getId())).isFalse();
    }
}
