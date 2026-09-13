package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.e.EagerCadet;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.StormCauldron;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AirElemental.class, EagerCadet.class, GrizzlyBears.class, Reprisal.class, SerraAngel.class, StormCauldron.class})
class ReprisalTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Reprisal destroys target creature with power 4+ and moves it to graveyard")
    void resolvingDestroysTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        harness.setHand(player1, List.of(new Reprisal()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Serra Angel");
        harness.assertInGraveyard(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Resolving Reprisal destroys target creature with power 4+ and moves it to graveyard")
    void resolvingDestroysTargetCreatureUpstreamReview() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.setHand(player1, List.of(new Reprisal()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Cannot target a creature with power less than 4")
    void cannotTargetSmallCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EagerCadet());

        harness.setHand(player1, List.of(new Reprisal()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
    }

    @Test
    @DisplayName("Cannot target a creature with power less than 4")
    void cannotTargetSmallCreatureUpstreamReview() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

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
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        harness.setHand(player1, List.of(new Reprisal()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        target.setPowerModifier(-1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Serra Angel");
        harness.assertNotInGraveyard(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Reprisal does nothing if the target's power becomes less than 4 before resolution")
    void fizzlesWhenTargetPowerBecomesTooSmallUpstreamReview() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.setHand(player1, List.of(new Reprisal()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        target.setPowerModifier(-1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Air Elemental");
        harness.assertNotInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Reprisal does not allow the destroyed creature to regenerate")
    void cannotRegenerate() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        target.setRegenerationShield(1);

        harness.setHand(player1, List.of(new Reprisal()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Serra Angel");
        harness.assertInGraveyard(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Reprisal does not allow the destroyed creature to regenerate")
    void cannotRegenerateUpstreamReview() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        target.setRegenerationShield(1);

        harness.setHand(player1, List.of(new Reprisal()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }
}
