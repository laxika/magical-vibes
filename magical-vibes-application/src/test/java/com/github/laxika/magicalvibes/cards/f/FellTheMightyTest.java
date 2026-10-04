package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.t.TrollAscetic;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FellTheMighty.class, AirElemental.class, GrizzlyBears.class, HillGiant.class,
        Forest.class, GiantGrowth.class, Unsummon.class, TrollAscetic.class})
class FellTheMightyTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys creatures with power greater than the target's power")
    void destroysCreaturesWithGreaterPower() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new AirElemental());
        harness.addToBattlefield(player2, new GrizzlyBears());
        var target = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new FellTheMighty()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castAndResolveSorcery(player1, 0, target);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new FellTheMighty()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        var target = harness.getPermanentId(player2, "Forest");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void usesTargetsPowerAfterResponseResolves() {
        var target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new FellTheMighty(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player1, "Fell the Mighty");
    }

    @Test
    void usesOtherCreaturesPowerAfterResponseResolves() {
        var target = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        var bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FellTheMighty()));
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void destroysNothingWhenTargetLeavesBattlefield() {
        var target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new FellTheMighty()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player1, "Fell the Mighty");
    }

    @Test
    void canTargetOpponentsCreatureAndDestroysHexproofCreaturesWithoutTargetingThem() {
        var target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new TrollAscetic());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new FellTheMighty()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Troll Ascetic");
        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertOnBattlefield(player1, "Forest");
    }
}
