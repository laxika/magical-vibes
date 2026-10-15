package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BriselaVoiceOfNightmares;
import com.github.laxika.magicalvibes.cards.b.BrunaTheFadingLight;
import com.github.laxika.magicalvibes.cards.g.GreaterAuramancy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiselaTheBrokenBlade;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LostDays.class, GreaterAuramancy.class, GrizzlyBears.class, Island.class, MindStone.class,
        BrunaTheFadingLight.class, GiselaTheBrokenBlade.class, BriselaVoiceOfNightmares.class})
class LostDaysTest extends BaseCardTest {

    @Test
    @DisplayName("The target's owner can put a creature second from the top and the caster creates a Clue")
    void targetOwnerChoosesSecondFromTop() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card topCard = new Island();
        Card bottomCard = new Island();
        harness.setLibrary(player2, List.of(topCard, bottomCard));

        castLostDays(target);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.TargetLibraryDestinationChoice.class)
                .options()).containsExactly("Second from the top", "Bottom");

        harness.handleListChoice(player2, "Second from the top");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, target.getCard(), bottomCard);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("The target's owner can put an enchantment on the bottom of their library")
    void targetOwnerChoosesBottomForEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreaterAuramancy());
        Card topCard = new Island();
        Card bottomCard = new Island();
        harness.setLibrary(player2, List.of(topCard, bottomCard));

        castLostDays(target);
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, bottomCard, target.getCard());
        harness.assertNotOnBattlefield(player2, "Greater Auramancy");
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target an artifact")
    void cannotTargetArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MindStone());
        harness.setHand(player1, List.of(new LostDays()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Second from the top puts the target into an empty library")
    void secondFromTopWithEmptyLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of());

        castLostDays(target);
        harness.handleListChoice(player2, "Second from the top");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard());
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("The owner chooses and receives a creature controlled by another player")
    void ownerChoosesForCreatureControlledByOpponent() {
        Card creature = new GrizzlyBears();
        creature.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player1, creature);
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));
        harness.setLibrary(player1, List.of());

        castLostDays(target);

        assertThatThrownBy(() -> harness.handleListChoice(player1, "Bottom"))
                .isInstanceOf(IllegalStateException.class);
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, creature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("An illegal sole target prevents the Clue from being created")
    void removedTargetPreventsClueCreation() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LostDays()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Lost Days");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The created Clue can be sacrificed for two mana to draw a card")
    void clueCanBeSacrificedToDraw() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card drawnCard = new Island();
        harness.setLibrary(player1, List.of(drawnCard));
        castLostDays(target);
        harness.handleListChoice(player2, "Bottom");
        Permanent clue = findPermanent(player1, "Clue");
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clue);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, clueIndex, null, null);
        harness.assertNotOnBattlefield(player1, "Clue");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The owner of a melded target must still choose its library destination")
    void meldedTargetOwnerChoosesDestination() {
        harness.addToBattlefield(player2, new GiselaTheBrokenBlade());
        harness.addToBattlefield(player2, new BrunaTheFadingLight());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);
        harness.passBothPriorities();
        Permanent target = findPermanent(player2, "Brisela, Voice of Nightmares");
        harness.setLibrary(player2, List.of(new Island(), new Island()));

        castLostDays(target);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.TargetLibraryDestinationChoice.class)
                .options()).containsExactly("Second from the top", "Bottom");
    }

    private void castLostDays(Permanent target) {
        harness.setHand(player1, List.of(new LostDays()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

}
