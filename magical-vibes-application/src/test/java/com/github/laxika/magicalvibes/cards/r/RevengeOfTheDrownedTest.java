package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({RevengeOfTheDrowned.class, GrizzlyBears.class, Island.class})
class RevengeOfTheDrownedTest extends BaseCardTest {

    @Test
    @DisplayName("Puts the target creature on the bottom and creates a decayed Zombie")
    void ownerChoosesBottomAndSpellCreatesZombie() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card libraryCard = new GrizzlyBears();
        setDeck(player2, List.of(libraryCard));

        castRevenge(target.getId());

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.TargetLibraryDestinationChoice.class)
                .playerId()).isEqualTo(player2.getId());

        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard, target.getCard());
        assertDecayedZombieCreated();
        harness.assertInGraveyard(player1, "Revenge of the Drowned");
    }

    @Test
    @DisplayName("Puts the target creature on top and creates a decayed Zombie")
    void ownerChoosesTopAndSpellCreatesZombie() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card libraryCard = new GrizzlyBears();
        setDeck(player2, List.of(libraryCard));

        castRevenge(target.getId());

        harness.handleListChoice(player2, "Top");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard(), libraryCard);
        assertDecayedZombieCreated();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Card land = new Island();
        Permanent target = harness.addToBattlefieldAndReturn(player2, land);
        harness.setHand(player1, List.of(new RevengeOfTheDrowned()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castRevenge(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new RevengeOfTheDrowned()));
        addMana();
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }

    private void assertDecayedZombieCreated() {
        Permanent zombie = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(zombie.getCard().getPower()).isEqualTo(2);
        assertThat(zombie.getCard().getToughness()).isEqualTo(2);
        assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(zombie.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(zombie.getCard().getKeywords()).contains(Keyword.DECAYED);
    }
}
