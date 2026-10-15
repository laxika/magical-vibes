package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.t.TangledIslet;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArtilleryBlast.class, AirElemental.class, Forest.class, Island.class, Plains.class, TangledIslet.class})
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

    @Test
    @DisplayName("Deals one damage with no lands, ignoring opponents' land types")
    void dealsOneDamageWithoutControlledLands() {
        Permanent target = addCreatureReady(player2, new AirElemental());
        target.tap();
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Plains());
        castArtilleryBlast(target);

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can damage a tapped creature you control")
    void canTargetOwnTappedCreature() {
        Permanent target = addCreatureReady(player1, new AirElemental());
        target.tap();
        castArtilleryBlast(target);

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Uses basic land types present when the spell resolves")
    void evaluatesDomainAtResolution() {
        Permanent target = addCreatureReady(player2, new AirElemental());
        target.tap();
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new ArtilleryBlast()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.addToBattlefield(player1, new Island());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not damage a target that untaps before resolution")
    void untappedTargetBecomesIllegal() {
        Permanent target = addCreatureReady(player2, new AirElemental());
        target.tap();
        harness.setHand(player1, List.of(new ArtilleryBlast()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, target.getId());
        target.untap();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Artillery Blast");
    }

    @Test
    @DisplayName("Cannot target a tapped land")
    void cannotTargetTappedNoncreature() {
        harness.addToBattlefield(player2, new Forest());
        Permanent target = findPermanent(player2, "Forest");
        target.tap();
        harness.setHand(player1, List.of(new ArtilleryBlast()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counts both basic land types on a nonbasic dual land")
    void countsTypesOnNonbasicLand() {
        Permanent target = addCreatureReady(player2, new AirElemental());
        target.tap();
        harness.addToBattlefield(player1, new TangledIslet());
        castArtilleryBlast(target);

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    private void castArtilleryBlast(Permanent target) {
        harness.setHand(player1, List.of(new ArtilleryBlast()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
