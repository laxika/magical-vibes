package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScroungeTest extends BaseCardTest {

    private void cast(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new Scrounge()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, targetPlayerId);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The targeted opponent chooses an artifact card to put onto the caster's battlefield")
    void targetedOpponentChoosesArtifact() {
        var firstArtifact = new DarksteelRelic();
        var secondArtifact = new TormodsCrypt();
        var nonArtifact = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(nonArtifact, firstArtifact, secondArtifact));

        cast(player2.getId());

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.cardPool()).containsExactly(firstArtifact, secondArtifact);

        harness.handleGraveyardCardChosen(player2, 1);

        harness.assertOnBattlefield(player1, "Tormod's Crypt");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Darksteel Relic");
    }

    @Test
    @DisplayName("A single artifact is put onto the battlefield without a choice")
    void singleArtifactIsChosenAutomatically() {
        harness.setGraveyard(player2, List.of(new DarksteelRelic()));

        cast(player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Darksteel Relic");
        harness.assertNotInGraveyard(player2, "Darksteel Relic");
    }

    @Test
    @DisplayName("The spell does nothing when the targeted opponent has no artifact cards")
    void noArtifactDoesNothing() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        cast(player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The spell cannot target its controller")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new Scrounge()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
