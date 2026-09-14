package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BambooGroveArcher.class, AirElemental.class, GrizzlyBears.class})
class BambooGroveArcherTest extends BaseCardTest {

    @Test
    @DisplayName("Channel destroys target creature with flying and discards Bamboo Grove Archer")
    void channelDestroysFlyingCreatureAndDiscardsSource() {
        harness.setHand(player1, List.of(new BambooGroveArcher()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
        harness.assertInGraveyard(player1, "Bamboo Grove Archer");
    }

    @Test
    @DisplayName("Channel cannot target a creature without flying")
    void channelRejectsNonFlyingCreature() {
        harness.setHand(player1, List.of(new BambooGroveArcher()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Bamboo Grove Archer");
    }
}
