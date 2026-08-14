package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TelJiladJusticeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target artifact and scries 2")
    void destroysArtifactAndScriesTwo() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        Card bottom = new Forest();
        Card top = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bottom, top));
        harness.setHand(player1, List.of(new TelJiladJustice()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fountain of Youth");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(bottom, top);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(top);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Tel-Jilad Justice");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new TelJiladJustice()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }
}
