package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AvenFlock;
import com.github.laxika.magicalvibes.cards.a.AvenShrine;
import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Execute.class, GloriousAnthem.class, GlorySeeker.class, GrizzlyBears.class, AvenFlock.class, DuskImp.class, AvenShrine.class})
class ExecuteTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Execute destroys a white creature and draws a card")
    void resolvingDestroysWhiteCreatureAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());

        harness.setHand(player1, List.of(new Execute()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glory Seeker");
        harness.assertInGraveyard(player2, "Glory Seeker");
        // Execute was cast from a one-card hand, so the only card in hand is the one drawn.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Execute destroys the creature even with a regeneration shield")
    void cannotBeRegenerated() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        target.setRegenerationShield(1);

        harness.setHand(player1, List.of(new Execute()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glory Seeker");
        harness.assertInGraveyard(player2, "Glory Seeker");
    }

    @Test
    @DisplayName("Cannot target a non-white creature")
    void cannotTargetNonWhiteCreature() {
        // A legal white target elsewhere keeps Execute playable, so the rejection is the filter message.
        harness.addToBattlefield(player1, new GlorySeeker());

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Execute()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("white creature");
    }

    @Test
    @DisplayName("Cannot target a white noncreature permanent")
    void cannotTargetWhiteNoncreaturePermanent() {
        harness.addToBattlefield(player1, new GlorySeeker());
        Permanent anthem = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        harness.setHand(player1, List.of(new Execute()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, anthem.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("white creature");
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
