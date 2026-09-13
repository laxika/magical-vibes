package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BrokenFall;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MoggFanatic;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.Regeneration;
import com.github.laxika.magicalvibes.cards.s.SkyshroudTroll;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AirElemental.class, BrokenFall.class, GrizzlyBears.class, MoggFanatic.class, Ornithopter.class, Perish.class, Regeneration.class, SkyshroudTroll.class})
class PerishTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys green creatures controlled by both players")
    void destroysGreenCreaturesFromBothPlayers() {
        harness.addToBattlefield(player1, new SkyshroudTroll());
        harness.addToBattlefield(player2, new SkyshroudTroll());

        harness.castFromHand(player1, new Perish(), "{2}{B}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Skyshroud Troll");
        harness.assertNotOnBattlefield(player2, "Skyshroud Troll");
        harness.assertInGraveyard(player1, "Skyshroud Troll");
        harness.assertInGraveyard(player2, "Skyshroud Troll");
    }

    @Test
    @DisplayName("Leaves non-green creatures and green noncreatures on the battlefield")
    void leavesNonGreenCreaturesAndGreenNoncreatures() {
        harness.addToBattlefield(player1, new MoggFanatic());
        harness.addToBattlefield(player1, new BrokenFall());
        harness.addToBattlefield(player2, new SkyshroudTroll());

        harness.castFromHand(player1, new Perish(), "{2}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mogg Fanatic");
        harness.assertOnBattlefield(player1, "Broken Fall");
        harness.assertNotOnBattlefield(player2, "Skyshroud Troll");
    }

    @Test
    @DisplayName("Leaves green noncreature permanents on the battlefield")
    void leavesGreenNoncreaturePermanents() {
        Permanent airElemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.setHand(player1, List.of(new Regeneration()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, airElemental.getId());
        harness.passBothPriorities();

        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Perish(), "{2}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Air Elemental");
        harness.assertOnBattlefield(player1, "Regeneration");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Green creatures with regeneration shields are destroyed")
    void destroysGreenCreaturesDespiteRegenerationShield() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setRegenerationShield(1);

        harness.castFromHand(player1, new Perish(), "{2}{B}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Perish goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.castFromHand(player1, new Perish(), "{2}{B}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Perish");
    }

    @Test
    @DisplayName("Does not allow regeneration to save a green creature")
    void doesNotAllowRegenerationToSaveGreenCreature() {
        harness.addToBattlefield(player2, new SkyshroudTroll());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Skyshroud Troll").getRegenerationShield()).isEqualTo(1);
        harness.castFromHand(player1, new Perish(), "{2}{B}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Skyshroud Troll");
        harness.assertInGraveyard(player2, "Skyshroud Troll");
    }

    @Test
    @DisplayName("Leaves non-green creatures on the battlefield")
    void leavesNonGreenCreatures() {
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Perish(), "{2}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Air Elemental");
        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }
}
