package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.Confiscate;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.r.RagavanNimblePilferer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.UnquestionedAuthority;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlimpseOfTomorrow.class, GrizzlyBears.class, Pacifism.class, Shock.class,
        Confiscate.class, RagavanNimblePilferer.class, UnquestionedAuthority.class})
class GlimpseOfTomorrowTest extends BaseCardTest {

    @Test
    void suspendShufflesOwnPermanentsAndReturnsNonAurasBeforeAuras() {
        GlimpseOfTomorrow glimpse = new GlimpseOfTomorrow();
        Card creatureCard = new GrizzlyBears();
        Card auraCard = new Pacifism();
        Card instantCard = new Shock();
        Card opponentLibraryCard = new Shock();

        harness.setHand(player1, List.of(glimpse));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addToBattlefield(player1, token("First token", CardType.CREATURE));
        harness.addToBattlefield(player1, token("Second token", CardType.CREATURE));
        harness.addToBattlefield(player1, token("Third token", CardType.CREATURE));
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, token("Opponent land", CardType.LAND));
        harness.setLibrary(player1, List.of(auraCard, creatureCard, instantCard));
        harness.setLibrary(player2, List.of(opponentLibraryCard));

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(glimpse);
        assertThat(gd.exiledCardTimeCounters).containsEntry(glimpse.getId(), 3);

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opponentLand);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentLibraryCard);
        Permanent creature = findPermanent(player1, creatureCard.getName());
        Permanent aura = findPermanent(player1, auraCard.getName());
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(instantCard);
    }

    @Test
    void aurasCannotEnchantOtherAurasFromTheSameReveal() {
        Card firstAura = new Confiscate();
        Card secondAura = new Confiscate();
        harness.addToBattlefield(player1, token("First token", CardType.CREATURE));
        harness.addToBattlefield(player1, token("Second token", CardType.CREATURE));
        Permanent land = harness.addToBattlefieldAndReturn(player2, token("Opponent land", CardType.LAND));
        harness.setLibrary(player1, List.of(firstAura, secondAura));

        resolveGlimpseFromSuspend();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanents(player1, firstAura.getName())).hasSize(2)
                .allSatisfy(aura -> assertThat(aura.getAttachedTo()).isEqualTo(land.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void unattachableAuraAndNonpermanentCardsRemainInLibrary() {
        Card aura = new Pacifism();
        Card instant = new Shock();
        harness.addToBattlefield(player1, token("First token", CardType.CREATURE));
        harness.addToBattlefield(player1, token("Second token", CardType.CREATURE));
        harness.setLibrary(player1, List.of(aura, instant));

        resolveGlimpseFromSuspend();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(aura, instant);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void countsOwnedPermanentsUnderOpponentControlButLeavesBorrowedPermanents() {
        Card ownedCreature = new GrizzlyBears();
        Card borrowedCreature = new GrizzlyBears();
        Permanent owned = harness.addToBattlefieldAndReturn(player2, ownedCreature);
        Permanent borrowed = harness.addToBattlefieldAndReturn(player1, borrowedCreature);
        gd.stolenCreatures.put(owned.getId(), player1.getId());
        gd.stolenCreatures.put(borrowed.getId(), player2.getId());
        harness.setLibrary(player1, List.of());

        resolveGlimpseFromSuspend();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2).contains(borrowed)
                .anySatisfy(permanent -> assertThat(permanent.getCard()).isSameAs(ownedCreature));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void revealsOnlyAvailableCardsWhenTokensOutnumberLibrary() {
        Card creature = new GrizzlyBears();
        Card instant = new Shock();
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, token("Token " + i, CardType.CREATURE));
        }
        harness.setLibrary(player1, List.of(creature, instant));

        resolveGlimpseFromSuspend();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1)
                .allSatisfy(permanent -> assertThat(permanent.getCard()).isSameAs(creature));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(instant);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void revealsNothingWhenNoPermanentsAreOwned() {
        Card creature = new GrizzlyBears();
        Card instant = new Shock();
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(creature, instant));

        resolveGlimpseFromSuspend();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opponentCreature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(creature, instant);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void auraEnterTriggerResolvesAfterRemainingCardsAreBottomed() {
        Card aura = new UnquestionedAuthority();
        Card instant = new Shock();
        harness.addToBattlefield(player1, token("First token", CardType.CREATURE));
        harness.addToBattlefield(player1, token("Second token", CardType.CREATURE));
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(aura, instant));

        resolveGlimpseFromSuspend();
        resolveAllTriggers();

        assertThat(findPermanent(player1, aura.getName()).getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(instant);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void choosesAuraAttachmentBeforeApplyingLegendRule() {
        Card aura = new Pacifism();
        harness.addToBattlefield(player1, token("First token", CardType.CREATURE));
        harness.addToBattlefield(player1, token("Second token", CardType.CREATURE));
        harness.addToBattlefield(player1, token("Third token", CardType.CREATURE));
        harness.setLibrary(player1, List.of(new RagavanNimblePilferer(), new RagavanNimblePilferer(), aura));

        resolveGlimpseFromSuspend();

        assertThat(gd.interaction.pendingAuraCard()).isSameAs(aura);
        assertThat(findPermanents(player1, "Ragavan, Nimble Pilferer")).hasSize(2);
    }

    private void resolveGlimpseFromSuspend() {
        harness.setHand(player1, List.of(new GlimpseOfTomorrow()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateHandAbility(player1, 0, null);
        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
    }

    private Card token(String name, CardType type) {
        Card token = new Card();
        token.setName(name);
        token.setType(type);
        token.setToken(true);
        if (type == CardType.CREATURE) {
            token.setPower(1);
            token.setToughness(1);
        }
        return token;
    }
}
