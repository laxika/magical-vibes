package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.w.WallOfStone;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RyuseiTheFallingStar.class, GrizzlyBears.class, HillGiant.class, AirElemental.class,
        WallOfStone.class})
class RyuseiTheFallingStarTest extends BaseCardTest {

    @Test
    @DisplayName("When Ryusei dies, it deals 5 damage to each creature without flying")
    void deathTriggerBurnsNonFlyers() {
        Permanent ryusei = harness.addToBattlefieldAndReturn(player1, new RyuseiTheFallingStar());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new AirElemental());

        ryusei.setMarkedDamage(5);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.runStateBasedActions();

        // Both non-flyers took 5 and died; the flyer is untouched.
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(findPermanents(player2, "Hill Giant")).isEmpty();
        assertThat(findPermanent(player2, "Air Elemental").getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("When Ryusei dies, it deals exactly 5 damage to a surviving non-flyer")
    void deathTriggerDealsExactlyFiveDamage() {
        Permanent ryusei = harness.addToBattlefieldAndReturn(player1, new RyuseiTheFallingStar());
        harness.addToBattlefield(player2, new WallOfStone());

        ryusei.setMarkedDamage(5);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Wall of Stone").getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("The death trigger checks flying when it resolves")
    void checksFlyingAtResolution() {
        Permanent ryusei = harness.addToBattlefieldAndReturn(player1, new RyuseiTheFallingStar());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        ryusei.setMarkedDamage(5);
        harness.runStateBasedActions();

        bears.getGrantedKeywords().add(Keyword.FLYING);
        elemental.getRemovedKeywords().add(Keyword.FLYING);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(bears.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Air Elemental");
        harness.assertNotOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("The death trigger damages creatures that enter before resolution but not players")
    void damagesNewCreaturesWithoutDamagingPlayers() {
        Permanent ryusei = harness.addToBattlefieldAndReturn(player1, new RyuseiTheFallingStar());
        harness.setLife(player1, 17);
        harness.setLife(player2, 13);

        ryusei.setMarkedDamage(5);
        harness.runStateBasedActions();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ryusei, the Falling Star");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 13);
    }
}
