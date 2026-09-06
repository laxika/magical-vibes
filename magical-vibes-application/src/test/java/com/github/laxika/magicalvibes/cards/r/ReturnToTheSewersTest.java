package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReturnToTheSewers.class, GrizzlyBears.class, Island.class})
class ReturnToTheSewersTest extends BaseCardTest {

    @Test
    @DisplayName("The target's owner can put it on top and the spell creates a Mutagen")
    void putsTargetOnTopAndCreatesMutagen() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card existingTop = new Island();
        setDeck(player2, List.of(existingTop));

        cast(target);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
        harness.handleListChoice(player2, "Top");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard(), existingTop);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(findMutagen(player1)).isNotNull();
    }

    @Test
    @DisplayName("The target's owner can put it on the bottom")
    void putsTargetOnBottom() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card existingTop = new Island();
        setDeck(player2, List.of(existingTop));

        cast(target);
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(existingTop, target.getCard());
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A Mutagen can be sacrificed to put a +1/+1 counter on a creature")
    void mutagenPutsCounterOnTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(target);
        harness.handleListChoice(player2, "Top");

        Permanent mutagen = findMutagen(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mutagen),
                0,
                null,
                recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getEffectivePower()).isEqualTo(3);
        assertThat(findMutagen(player1)).isNull();
    }

    @Test
    @DisplayName("Return to the Sewers cannot target a land")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new ReturnToTheSewers()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new ReturnToTheSewers()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent findMutagen(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Mutagen"))
                .findFirst()
                .orElse(null);
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
