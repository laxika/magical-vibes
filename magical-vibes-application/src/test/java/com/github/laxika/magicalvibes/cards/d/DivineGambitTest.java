package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DivineGambitTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles an opponent's artifact and offers a permanent card from their hand")
    void exilesArtifactAndOffersPermanentFromHand() {
        harness.addToBattlefield(player2, new Spellbook());
        UUID targetId = harness.getPermanentId(player2, "Spellbook");
        harness.setHand(player1, List.of(new DivineGambit()));
        harness.setHand(player2, List.of(new Forest(), new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class).validIndices())
                .containsExactly(0);
        harness.assertNotOnBattlefield(player2, "Spellbook");

        harness.handleCardChosen(player2, 0);

        harness.assertOnBattlefield(player2, "Forest");
        harness.assertInHand(player2, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The opponent may decline to put a permanent onto the battlefield")
    void opponentMayDeclinePermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new DivineGambit()));
        harness.setHand(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, -1);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a permanent the caster controls")
    void cannotTargetOwnPermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new DivineGambit()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
