package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StolenIdentityTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a token copy of target artifact")
    void createsTokenCopyOfArtifact() {
        harness.addToBattlefield(player2, new DarksteelRelic());
        harness.setHand(player1, List.of(new StolenIdentity()));
        addStolenIdentityMana();

        UUID targetId = harness.getPermanentId(player2, "Darksteel Relic");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Darksteel Relic"))
                .filter(permanent -> permanent.getCard().isToken()))
                .hasSize(1);
    }

    @Test
    @DisplayName("Rejects a non-artifact non-creature target")
    void rejectsInvalidTarget() {
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new StolenIdentity()));
        addStolenIdentityMana();

        UUID targetId = harness.getPermanentId(player1, "Mountain");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can encode the resolving spell on the token it creates")
    void canEncodeOnCreatedToken() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StolenIdentity()));
        addStolenIdentityMana();

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        harness.handlePermanentChosen(player1, token.getId());

        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getName().equals("Stolen Identity"));
        harness.assertNotInGraveyard(player1, "Stolen Identity");
    }

    private void addStolenIdentityMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
