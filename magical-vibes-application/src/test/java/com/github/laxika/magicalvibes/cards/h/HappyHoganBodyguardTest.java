package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HappyHoganBodyguard.class, GrizzlyBears.class, Island.class, DryadArbor.class})
class HappyHoganBodyguardTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts the target second from the top when its owner chooses that option")
    void targetOwnerChoosesSecondFromTop() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card topCard = new Island();
        Card bottomCard = new Island();
        harness.setLibrary(player2, List.of(topCard, bottomCard));

        castHappyHogan(target);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);

        harness.handleListChoice(player2, "Second from the top");

        assertThat(gd.playerDecks.get(player2.getId()))
                .containsExactly(topCard, target.getCard(), bottomCard);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB lets the target's owner put the target on the bottom")
    void targetOwnerChoosesBottom() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card topCard = new Island();
        Card bottomCard = new Island();
        harness.setLibrary(player2, List.of(topCard, bottomCard));

        castHappyHogan(target);
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerDecks.get(player2.getId()))
                .containsExactly(topCard, bottomCard, target.getCard());
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB cannot target a creature you control")
    void cannotTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HappyHoganBodyguard()));
        addHappyHoganMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("ETB can put an opposing land creature into its owner's library")
    void canTargetLandCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DryadArbor());
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));

        castHappyHogan(target);
        harness.handleListChoice(player2, "Second from the top");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, target.getCard());
        harness.assertNotOnBattlefield(player2, "Dryad Arbor");
    }

    @Test
    @DisplayName("Second from the top puts the creature into an empty library")
    void secondFromTopOfEmptyLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of());

        castHappyHogan(target);
        harness.handleListChoice(player2, "Second from the top");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard());
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The owner chooses and receives a creature controlled by an opponent")
    void ownerRatherThanControllerChoosesDestination() {
        Card creature = new GrizzlyBears();
        creature.setOwnerId(player1.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, creature);
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLibrary(player2, List.of());

        castHappyHogan(target);
        assertThatThrownBy(() -> harness.handleListChoice(player2, "Bottom"))
                .isInstanceOf(IllegalStateException.class);
        harness.handleListChoice(player1, "Bottom");

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, creature);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Happy Hogan enters even when no opponent controls a creature")
    void entersWithoutLegalTargets() {
        harness.castFromHand(player1, new HappyHoganBodyguard(), "{5}{U}");

        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Happy Hogan, Bodyguard");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void castHappyHogan(Permanent target) {
        harness.setHand(player1, List.of(new HappyHoganBodyguard()));
        addHappyHoganMana();
        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();
    }

    private void addHappyHoganMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
