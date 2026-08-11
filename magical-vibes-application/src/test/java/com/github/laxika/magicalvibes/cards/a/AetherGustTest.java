package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AetherGustTest extends BaseCardTest {

    @Test
    @DisplayName("The target's owner may put a red permanent on top")
    void targetOwnerPutsRedPermanentOnTop() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        setLibrary(player2, new GrizzlyBears(), new Shock());
        castAt(target);

        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.options()).containsExactly("Put it on top", "Put it on the bottom");

        harness.handleListChoice(player2, "Put it on top");

        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Hill Giant");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Aether Gust");
    }

    @Test
    @DisplayName("The target's owner may put a green permanent on the bottom")
    void targetOwnerPutsGreenPermanentOnBottom() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        setLibrary(player2, new HillGiant(), new Shock());
        castAt(target);

        harness.passBothPriorities();
        harness.handleListChoice(player2, "Put it on the bottom");

        List<Card> library = gd.playerDecks.get(player2.getId());
        assertThat(library).extracting(Card::getName)
                .containsExactly("Hill Giant", "Shock", "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Aether Gust can target a red spell")
    void targetsRedSpell() {
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new AetherGust()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        setLibrary(player2, new GrizzlyBears());

        harness.castInstant(player2, 0, player1.getId());
        harness.castInstant(player1, 0, shock.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleListChoice(player2, "Put it on the bottom");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears", "Shock");
    }

    @Test
    @DisplayName("Aether Gust rejects a blue permanent")
    void rejectsBluePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new com.github.laxika.magicalvibes.cards.a.AirElemental());
        harness.setHand(player1, List.of(new AetherGust()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castAt(Permanent target) {
        harness.setHand(player1, List.of(new AetherGust()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
    }

    private void setLibrary(com.github.laxika.magicalvibes.model.Player player, Card... cards) {
        gd.playerDecks.put(player.getId(), new ArrayList<>(List.of(cards)));
    }
}
