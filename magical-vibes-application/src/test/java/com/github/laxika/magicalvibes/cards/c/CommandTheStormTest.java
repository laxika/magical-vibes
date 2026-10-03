package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HatcherySpider;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.u.UnexplainedDisappearance;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CommandTheStorm.class, GrizzlyBears.class, LeoninScimitar.class,
        HatcherySpider.class, UnexplainedDisappearance.class})
class CommandTheStormTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage to target creature, destroying it")
    void dealsFiveDamageToTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CommandTheStorm()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Command the Storm");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new CommandTheStorm()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Leonin Scimitar");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Deals exactly 5 damage to a surviving creature you control")
    void dealsExactlyFiveDamageToOwnCreature() {
        var target = harness.addToBattlefieldAndReturn(player1, new HatcherySpider());
        harness.setHand(player1, List.of(new CommandTheStorm()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hatchery Spider");
        assertThat(target.getMarkedDamage()).isEqualTo(5);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Command the Storm");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new CommandTheStorm()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not deal damage when its target leaves before resolution")
    void targetLeavingBeforeResolution() {
        var target = harness.addToBattlefieldAndReturn(player2, new HatcherySpider());
        harness.setHand(player1, List.of(new CommandTheStorm()));
        harness.setHand(player2, List.of(new UnexplainedDisappearance()));
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player2, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertNotOnBattlefield(player2, "Hatchery Spider");
        harness.assertInHand(player2, "Hatchery Spider");
        harness.assertNotInGraveyard(player2, "Hatchery Spider");
        harness.assertInGraveyard(player1, "Command the Storm");
        harness.assertInGraveyard(player2, "Unexplained Disappearance");
    }
}
