package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Whoosh.class, GrizzlyBears.class, Island.class, Spellbook.class})
class WhooshTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target creature without kicker")
    void returnsTargetCreatureWithoutKicker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castWhoosh(false, target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Returns a target permanent and draws a card when kicked")
    void returnsTargetPermanentAndDrawsWhenKicked() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castWhoosh(true, target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Returns a target artifact")
    void returnsTargetArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        castWhoosh(false, target);

        harness.assertNotOnBattlefield(player2, "Spellbook");
        harness.assertInHand(player2, "Spellbook");
    }

    @Test
    @DisplayName("Rejects a land target")
    void rejectsLandTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new Whoosh()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }

    @Test
    void returnsOwnPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        castWhoosh(false, target);

        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertInHand(player1, "Spellbook");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void kickedSpellReturnsArtifactAndDrawsExactlyOneCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        castWhoosh(true, target);

        harness.assertNotOnBattlefield(player2, "Spellbook");
        harness.assertInHand(player2, "Spellbook");
        harness.assertInHand(player1, "Island");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Whoosh!");
    }

    @Test
    void kickedSpellDoesNotDrawWhenTargetLeavesInResponse() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Whoosh()));
        harness.setHand(player2, List.of(new Whoosh()));
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castKickedInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.assertInHand(player2, "Grizzly Bears");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Whoosh!");
        harness.assertInGraveyard(player2, "Whoosh!");
    }

    private void castWhoosh(boolean kicked, Permanent target) {
        harness.setHand(player1, List.of(new Whoosh()));
        harness.addMana(player1, ManaColor.BLUE, kicked ? 2 : 1);
        harness.addMana(player1, ManaColor.COLORLESS, kicked ? 2 : 1);

        if (kicked) {
            harness.castKickedInstant(player1, 0, target.getId());
            harness.passBothPriorities();
        } else {
            harness.castAndResolveInstant(player1, 0, target.getId());
        }
    }
}
