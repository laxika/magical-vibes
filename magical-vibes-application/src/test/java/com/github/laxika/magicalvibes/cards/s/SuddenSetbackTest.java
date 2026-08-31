package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuddenSetback.class, AngelicChorus.class, GrizzlyBears.class, Island.class, Unsummon.class})
class SuddenSetbackTest extends BaseCardTest {

    @Test
    void nonlandPermanentOwnerCanChooseBottom() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelicChorus());
        Card topCard = new Island();
        setDeck(player2, List.of(topCard));

        castSuddenSetback(target.getId());

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)
                .playerId()).isEqualTo(player2.getId());

        harness.handleListChoice(player2, "Put it on the bottom");

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, target.getCard());
        harness.assertInGraveyard(player1, "Sudden Setback");
    }

    @Test
    void nonRedSpellOwnerCanChooseTop() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card topCard = new Island();
        setDeck(player2, List.of(topCard));
        Unsummon unsummon = new Unsummon();

        harness.setHand(player2, List.of(unsummon));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, target.getId());

        UUID spellId = unsummon.getId();
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new SuddenSetback()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, spellId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player2, "Put it on top");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(unsummon, topCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(target);
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Sudden Setback");
    }

    @Test
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new SuddenSetback()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castSuddenSetback(UUID targetId) {
        harness.setHand(player1, List.of(new SuddenSetback()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
