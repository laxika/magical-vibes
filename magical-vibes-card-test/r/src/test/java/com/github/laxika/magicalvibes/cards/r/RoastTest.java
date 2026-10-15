package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AncientCarp;
import com.github.laxika.magicalvibes.cards.d.DanceOfTheSkywise;
import com.github.laxika.magicalvibes.cards.g.Glint;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Roast.class, GrizzlyBears.class, WindDrake.class, AncientCarp.class,
        DanceOfTheSkywise.class, Glint.class})
class RoastTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage to a target creature without flying")
    void dealsDamageToNonFlyingCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Roast()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature with flying")
    void cannotTargetFlyingCreature() {
        harness.addToBattlefield(player2, new WindDrake());
        harness.setHand(player1, List.of(new Roast()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, harness.getPermanentId(player2, "Wind Drake")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Five damage is lethal to a creature with five toughness")
    void killsCreatureWithFiveToughness() {
        Permanent carp = harness.addToBattlefieldAndReturn(player2, new AncientCarp());
        harness.setHand(player1, List.of(new Roast()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, carp.getId());

        harness.assertNotOnBattlefield(player2, "Ancient Carp");
        harness.assertInGraveyard(player2, "Ancient Carp");
        harness.assertInGraveyard(player1, "Roast");
    }

    @Test
    @DisplayName("Deals exactly five damage to a friendly creature even after it gains hexproof")
    void dealsExactlyFiveDamageToFriendlyCreature() {
        Permanent carp = harness.addToBattlefieldAndReturn(player1, new AncientCarp());
        harness.setHand(player1, List.of(new Roast(), new Glint()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, carp.getId());
        harness.castAndResolveInstant(player1, 0, carp.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ancient Carp");
        assertThat(carp.getMarkedDamage()).isEqualTo(5);
        harness.assertInGraveyard(player1, "Roast");
    }

    @Test
    @DisplayName("Does not deal damage if its target gains flying before resolution")
    void targetGainingFlyingBecomesIllegal() {
        Permanent carp = harness.addToBattlefieldAndReturn(player2, new AncientCarp());
        harness.setHand(player1, List.of(new Roast()));
        harness.setHand(player2, List.of(new DanceOfTheSkywise()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, carp.getId());
        harness.castAndResolveInstant(player2, 0, carp.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Ancient Carp");
        assertThat(carp.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Roast");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new Roast()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
