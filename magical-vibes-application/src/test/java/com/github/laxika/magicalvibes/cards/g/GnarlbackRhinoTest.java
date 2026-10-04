package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GnarlbackRhino.class, Shock.class})
class GnarlbackRhinoTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when you cast a spell targeting Gnarlback Rhino")
    void drawsWhenTargetedByOwnSpell() {
        harness.addToBattlefield(player1, new GnarlbackRhino());
        UUID rhinoId = harness.getPermanentId(player1, "Gnarlback Rhino");

        harness.setHand(player1, List.of(new Shock()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, rhinoId);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Does not draw a card when you cast a spell targeting another creature")
    @CardUsed({GrizzlyBears.class})
    void doesNotDrawWhenAnotherCreatureIsTargeted() {
        harness.addToBattlefield(player1, new GnarlbackRhino());
        UUID otherCreatureId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();

        harness.setHand(player1, List.of(new Shock()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, otherCreatureId);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore - 1);
    }

    @Test
    @DisplayName("An opponent's spell targeting Rhino does not trigger a draw")
    void doesNotDrawForOpponentSpell() {
        UUID rhinoId = harness.addToBattlefieldAndReturn(player1, new GnarlbackRhino()).getId();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, rhinoId);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only the targeted Rhino triggers, and the draw resolves before the spell")
    void onlyTargetedRhinoDrawsBeforeSpellResolves() {
        var targetedRhino = harness.addToBattlefieldAndReturn(player1, new GnarlbackRhino());
        harness.addToBattlefield(player1, new GnarlbackRhino());
        harness.setHand(player1, List.of(new Shock()));
        Shock drawnCard = new Shock();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, targetedRhino.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).hasSize(1);
        assertThat(targetedRhino.getMarkedDamage()).isZero();
        harness.passBothPriorities();
        assertThat(targetedRhino.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }
}
