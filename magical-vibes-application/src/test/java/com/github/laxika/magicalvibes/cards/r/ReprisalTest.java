package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.p.PhyrexianWarBeast;
import com.github.laxika.magicalvibes.cards.s.SoldeviSteamBeast;
import com.github.laxika.magicalvibes.cards.s.StormCauldron;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Reprisal.class, SoldeviSteamBeast.class, PhyrexianWarBeast.class, StormCauldron.class})
class ReprisalTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Reprisal destroys target creature with power 4+ and moves it to graveyard")
    void resolvingDestroysTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SoldeviSteamBeast());

        harness.setHand(player1, List.of(new Reprisal()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Soldevi Steam Beast");
        harness.assertInGraveyard(player2, "Soldevi Steam Beast");
    }

    @Test
    @DisplayName("Cannot target a creature with power less than 4")
    void cannotTargetSmallCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PhyrexianWarBeast());

        harness.setHand(player1, List.of(new Reprisal()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StormCauldron());

        harness.setHand(player1, List.of(new Reprisal()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
    }

    @Test
    @DisplayName("Reprisal does nothing if the target's power becomes less than 4 before resolution")
    void fizzlesWhenTargetPowerBecomesTooSmall() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SoldeviSteamBeast());

        harness.setHand(player1, List.of(new Reprisal()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        target.setPowerModifier(-1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Soldevi Steam Beast");
        harness.assertNotInGraveyard(player2, "Soldevi Steam Beast");
    }

    @Test
    @DisplayName("Reprisal does not allow the destroyed creature to regenerate")
    void cannotRegenerate() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SoldeviSteamBeast());
        target.setRegenerationShield(1);

        harness.setHand(player1, List.of(new Reprisal()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Soldevi Steam Beast");
        harness.assertInGraveyard(player2, "Soldevi Steam Beast");
    }
}
