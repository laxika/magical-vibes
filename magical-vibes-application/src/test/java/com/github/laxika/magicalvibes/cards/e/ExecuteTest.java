package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AvenFlock;
import com.github.laxika.magicalvibes.cards.a.AvenShrine;
import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Execute.class, AvenFlock.class, DuskImp.class, AvenShrine.class})
class ExecuteTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Execute destroys a white creature and draws a card")
    void resolvingDestroysWhiteCreatureAndDraws() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player2, new AvenFlock());

        harness.setHand(player1, List.of(new Execute()));
        harness.setLibrary(player1, List.of(new DuskImp()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, hawk.getId());

        harness.assertNotOnBattlefield(player2, "Aven Flock");
        harness.assertInGraveyard(player2, "Aven Flock");
        harness.assertInHand(player1, "Dusk Imp");
    }

    @Test
    @DisplayName("Execute destroys the creature even with a regeneration shield")
    void cannotBeRegenerated() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player2, new AvenFlock());
        hawk.setRegenerationShield(1);

        harness.setHand(player1, List.of(new Execute()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, hawk.getId());

        harness.assertNotOnBattlefield(player2, "Aven Flock");
        harness.assertInGraveyard(player2, "Aven Flock");
    }

    @Test
    @DisplayName("Cannot target a non-white creature")
    void cannotTargetNonWhiteCreature() {
        // A legal white target elsewhere keeps Execute playable, so the rejection is the filter message.
        harness.addToBattlefield(player1, new AvenFlock());

        Permanent imp = harness.addToBattlefieldAndReturn(player2, new DuskImp());

        harness.setHand(player1, List.of(new Execute()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, imp.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("white creature");
    }

    @Test
    @DisplayName("Cannot target a white noncreature permanent")
    void cannotTargetWhiteNoncreaturePermanent() {
        // A legal white creature elsewhere keeps Execute playable, so the rejection is the creature filter.
        harness.addToBattlefield(player1, new AvenFlock());

        Permanent shrine = harness.addToBattlefieldAndReturn(player2, new AvenShrine());

        harness.setHand(player1, List.of(new Execute()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, shrine.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("white creature");
        harness.assertOnBattlefield(player2, "Aven Shrine");
    }

    @Test
    @DisplayName("Execute fizzles without drawing if its target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player2, new AvenFlock());

        harness.setHand(player1, List.of(new Execute()));
        harness.setLibrary(player1, List.of(new DuskImp()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, hawk.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertInGraveyard(player1, "Execute");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
