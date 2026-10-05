package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.cards.a.AxebaneGuardian;
import com.github.laxika.magicalvibes.cards.p.PlatinumEmperion;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LaunchParty.class, AxebaneGuardian.class, DrudgeBeetle.class, Forest.class, PlatinumEmperion.class})
class LaunchPartyTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("Destroys target creature and its controller loses 2 life")
    void destroysTargetAndControllerLosesTwoLife() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new AxebaneGuardian());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new DrudgeBeetle());

        harness.setHand(player1, List.of(new LaunchParty()));
        giveMana();

        harness.setLife(player2, 20);

        harness.castInstantWithSacrifice(player1, 0, victim.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Drudge Beetle");
        harness.assertInGraveyard(player2, "Drudge Beetle");
        harness.assertInGraveyard(player1, "Axebane Guardian");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Targeting your own creature makes you lose the 2 life")
    void targetingOwnCreatureHitsYou() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new AxebaneGuardian());
        Permanent victim = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());

        harness.setHand(player1, List.of(new LaunchParty()));
        giveMana();

        harness.setLife(player1, 20);

        harness.castInstantWithSacrifice(player1, 0, victim.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Drudge Beetle");
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Cannot cast Launch Party without a creature to sacrifice")
    void cannotCastWithoutSacrifice() {
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new DrudgeBeetle());

        harness.setHand(player1, List.of(new LaunchParty()));
        giveMana();

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, victim.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new AxebaneGuardian());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new LaunchParty()));
        giveMana();

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, land.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifice is paid before resolution")
    void sacrificeIsPaidBeforeResolution() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new AxebaneGuardian());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new DrudgeBeetle());
        harness.setHand(player1, List.of(new LaunchParty()));
        giveMana();
        harness.setLife(player2, 20);

        harness.castInstantWithSacrifice(player1, 0, victim.getId(), sacrifice.getId());

        harness.assertNotOnBattlefield(player1, "Axebane Guardian");
        harness.assertInGraveyard(player1, "Axebane Guardian");
        harness.assertOnBattlefield(player2, "Drudge Beetle");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Drudge Beetle");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Sacrificing the targeted creature makes the spell fail to resolve")
    void canSacrificeTheTargetWithoutLosingLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());
        harness.setHand(player1, List.of(new LaunchParty()));
        giveMana();
        harness.setLife(player1, 20);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Drudge Beetle");
        harness.assertInGraveyard(player1, "Launch Party");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Destroys Platinum Emperion before its controller loses life")
    void destroysLifeProtectionBeforeLifeLoss() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new AxebaneGuardian());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new PlatinumEmperion());
        harness.setHand(player1, List.of(new LaunchParty()));
        giveMana();
        harness.setLife(player2, 20);

        harness.castInstantWithSacrifice(player1, 0, victim.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Platinum Emperion");
        harness.assertLife(player2, 18);
    }
}
