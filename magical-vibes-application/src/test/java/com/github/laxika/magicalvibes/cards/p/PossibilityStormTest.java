package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.d.Demolish;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PossibilityStorm.class, CounselOfTheSoratami.class, Forest.class, GrizzlyBears.class, Demolish.class})
class PossibilityStormTest extends BaseCardTest {

    private UUID setUpStorm(List<Card> player1Library) {
        harness.addToBattlefield(player1, new PossibilityStorm());
        harness.setLibrary(player1, player1Library);
        return harness.getPermanentId(player1, "Possibility Storm");
    }

    private void castCounsel() {
        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
    }

    @Test
    @DisplayName("Casting a spell from hand puts the trigger on the stack above it")
    void castFromHandTriggers() {
        setUpStorm(List.of(new Forest(), new GrizzlyBears(), new CounselOfTheSoratami()));

        castCounsel();

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
    }

    @Test
    @DisplayName("Trigger exiles the spell and digs until a card sharing a type is exiled")
    void triggerExilesSpellAndDigsToSharedType() {
        UUID stormId = setUpStorm(List.of(new Forest(), new GrizzlyBears(), new CounselOfTheSoratami()));

        castCounsel();
        harness.passBothPriorities();

        // The sorcery left the stack; the land, the creature and the second sorcery are all exiled.
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.SORCERY_SPELL);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(stormId)).hasSize(4);
    }

    @Test
    @DisplayName("Accepting the offer casts the exiled card for free and bottoms the rest")
    void acceptingCastsExiledCardForFree() {
        UUID stormId = setUpStorm(List.of(new Forest(), new GrizzlyBears(), new CounselOfTheSoratami()));

        castCounsel();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        // The three cards not cast went back to the bottom of the library.
        assertThat(gd.getCardsExiledByPermanent(stormId)).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);

        // The freed sorcery is on the stack — cast from exile, so it does not re-trigger the storm.
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);

        harness.passBothPriorities();

        // Counsel of the Soratami drew two of the three re-bottomed cards.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining bottoms every exiled card, including the original spell")
    void decliningBottomsEverything() {
        UUID stormId = setUpStorm(List.of(new Forest(), new GrizzlyBears(), new CounselOfTheSoratami()));

        castCounsel();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getCardsExiledByPermanent(stormId)).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("No card shares a type — the whole library and the spell go back on the bottom")
    void noSharedTypeBottomsEverything() {
        UUID stormId = setUpStorm(List.of(new Forest(), new GrizzlyBears()));

        castCounsel();
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(stormId)).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A second Storm still searches after the first Storm exiles the original spell")
    void secondStormStillSearchesWithoutOriginalSpell() {
        setUpStorm(List.of(new CounselOfTheSoratami(), new CounselOfTheSoratami(), new Forest()));
        harness.addToBattlefield(player1, new PossibilityStorm());

        castCounsel();
        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An uncastable matching card is bottomed even when the player accepts")
    void matchingCardWithoutLegalTargetsIsBottomed() {
        UUID stormId = setUpStorm(List.of(new Forest(), new Demolish()));

        castCounsel();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(stormId)).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("An opponent's spell searches that opponent's library and stops at the first match")
    void opponentUsesOwnLibraryAndChoosesWhetherToCast() {
        UUID stormId = setUpStorm(List.of(new GrizzlyBears()));
        Forest untouched = new Forest();
        harness.setLibrary(player2, List.of(new Forest(), new CounselOfTheSoratami(), untouched));
        harness.setHand(player2, List.of(new CounselOfTheSoratami()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player2);

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(untouched);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.getCardsExiledByPermanent(stormId)).hasSize(3);
        harness.handleMayAbilityChosen(player2, false);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.getCardsExiledByPermanent(stormId)).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
