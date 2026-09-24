package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArtilleryBlast.class, AirElemental.class, Forest.class, Island.class, Plains.class})
class ArtilleryBlastTest extends BaseCardTest {

    @Test
    @DisplayName("Deals one damage plus one for each basic land type you control")
    void dealsOnePlusDomainDamage() {
        Permanent target = addCreatureReady(player2, new AirElemental());
        target.tap();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Plains());
        castArtilleryBlast(target);

        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Counts duplicate basic land types only once")
    void countsDistinctBasicLandTypes() {
        Permanent target = addCreatureReady(player2, new AirElemental());
        target.tap();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        castArtilleryBlast(target);

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        Permanent target = addCreatureReady(player2, new AirElemental());
        harness.setHand(player1, List.of(new ArtilleryBlast()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Target must be a tapped creature");
    }

    private void castArtilleryBlast(Permanent target) {
        harness.setHand(player1, List.of(new ArtilleryBlast()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
