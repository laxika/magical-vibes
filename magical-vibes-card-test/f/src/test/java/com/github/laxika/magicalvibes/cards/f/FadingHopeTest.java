package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CandlelitCavalry;
import com.github.laxika.magicalvibes.cards.d.DawnhartMentor;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LarderZombie;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FadingHope.class, CandlelitCavalry.class, DawnhartMentor.class, Island.class, LarderZombie.class})
class FadingHopeTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature with mana value 3 or less and offers scry 1")
    void returnsLowManaValueCreatureAndScries() {
        harness.addToBattlefield(player2, new DawnhartMentor());
        castFadingHope(harness.getPermanentId(player2, "Dawnhart Mentor"));

        harness.assertNotOnBattlefield(player2, "Dawnhart Mentor");
        harness.assertInHand(player2, "Dawnhart Mentor");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    @Test
    @DisplayName("Returns a creature with mana value greater than 3 without scrying")
    void returnsHighManaValueCreatureWithoutScrying() {
        harness.addToBattlefield(player2, new CandlelitCavalry());
        castFadingHope(harness.getPermanentId(player2, "Candlelit Cavalry"));

        harness.assertNotOnBattlefield(player2, "Candlelit Cavalry");
        harness.assertInHand(player2, "Candlelit Cavalry");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Scry 1 can put the top card on the bottom")
    void scryCanPutTopCardOnBottom() {
        harness.addToBattlefield(player2, new DawnhartMentor());
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop = deck.getFirst();
        castFadingHope(harness.getPermanentId(player2, "Dawnhart Mentor"));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(deck.getLast()).isSameAs(originalTop);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Fading Hope");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new FadingHope()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Island")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Scry 1 can keep the top card and looks only at the caster's library")
    void scryCanKeepTopCard() {
        Card top = new Island();
        Card next = new FadingHope();
        Card opponentTop = new Island();
        harness.setLibrary(player1, List.of(top, next));
        harness.setLibrary(player2, List.of(opponentTop));
        harness.addToBattlefield(player2, new DawnhartMentor());

        castFadingHope(harness.getPermanentId(player2, "Dawnhart Mentor"));

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.playerId()).isEqualTo(player1.getId());
        assertThat(scry.cards()).containsExactly(top);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, next);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTop);
        harness.assertInGraveyard(player1, "Fading Hope");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature with mana value below 3 also allows scrying")
    void returnsLowerManaValueCreatureAndScries() {
        harness.addToBattlefield(player2, new LarderZombie());

        castFadingHope(harness.getPermanentId(player2, "Larder Zombie"));

        harness.assertNotOnBattlefield(player2, "Larder Zombie");
        harness.assertInHand(player2, "Larder Zombie");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    @Test
    @DisplayName("Can return the caster's own creature and scry")
    void returnsOwnCreatureAndScries() {
        harness.addToBattlefield(player1, new DawnhartMentor());

        castFadingHope(harness.getPermanentId(player1, "Dawnhart Mentor"));

        harness.assertNotOnBattlefield(player1, "Dawnhart Mentor");
        harness.assertInHand(player1, "Dawnhart Mentor");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    @Test
    @DisplayName("Returning a low mana value creature with an empty library completes without a prompt")
    void emptyLibraryDoesNotPreventReturningCreature() {
        harness.setLibrary(player1, List.of());
        harness.addToBattlefield(player2, new DawnhartMentor());

        castFadingHope(harness.getPermanentId(player2, "Dawnhart Mentor"));

        harness.assertNotOnBattlefield(player2, "Dawnhart Mentor");
        harness.assertInHand(player2, "Dawnhart Mentor");
        harness.assertInGraveyard(player1, "Fading Hope");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not scry when its target leaves the battlefield before resolution")
    void illegalTargetPreventsScrying() {
        var target = harness.addToBattlefieldAndReturn(player2, new DawnhartMentor());
        Card top = new Island();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new FadingHope()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, target));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        harness.assertInGraveyard(player1, "Fading Hope");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returns a stolen creature to its owner rather than its controller")
    void returnsStolenCreatureToOwner() {
        var target = harness.addToBattlefieldAndReturn(player2, new DawnhartMentor());
        gd.stolenCreatures.put(target.getId(), player1.getId());

        castFadingHope(target.getId());

        harness.assertNotOnBattlefield(player2, "Dawnhart Mentor");
        harness.assertInHand(player1, "Dawnhart Mentor");
        harness.assertNotInHand(player2, "Dawnhart Mentor");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    @Test
    @DisplayName("Returning a creature token with no mana cost still allows scrying")
    void returnsTokenAndScries() {
        harness.enterBattlefieldAndReturn(player2, new DawnhartMentor());
        harness.passBothPriorities();
        var tokenId = harness.getPermanentId(player2, "Human");

        castFadingHope(tokenId);

        harness.assertNotOnBattlefield(player2, "Human");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.assertNotInHand(player2, "Human");
        harness.assertInGraveyard(player1, "Fading Hope");
        assertThat(gd.stack).isEmpty();
    }

    private void castFadingHope(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new FadingHope()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
