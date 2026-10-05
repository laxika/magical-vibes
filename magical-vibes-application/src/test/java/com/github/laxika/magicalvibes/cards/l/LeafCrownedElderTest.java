package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.m.MosquitoGuard;
import com.github.laxika.magicalvibes.cards.n.NamelessInversion;
import com.github.laxika.magicalvibes.cards.r.ReachOfBranches;
import com.github.laxika.magicalvibes.cards.v.VioletPall;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeafCrownedElder.class, MosquitoGuard.class, ReachOfBranches.class,
        VioletPall.class, NamelessInversion.class})
class LeafCrownedElderTest extends BaseCardTest {

    @Test
    @DisplayName("Kinship prompts to reveal when the top card shares a creature type")
    void kinshipPromptsWhenSharedType() {
        addCreatureReady(player1, new LeafCrownedElder());
        setLibraryTop(new LeafCrownedElder()); // Treefolk Shaman — shares a type

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("No reveal prompt when the top card shares no creature type")
    void noSharedTypeNoPrompt() {
        addCreatureReady(player1, new LeafCrownedElder());
        setLibraryTop(new MosquitoGuard()); // Kithkin Soldier — no shared type

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Revealing then playing casts the top card without paying its mana cost")
    void revealAndPlayCastsForFree() {
        addCreatureReady(player1, new LeafCrownedElder());
        setLibraryTop(new LeafCrownedElder());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true); // reveal
        harness.handleMayAbilityChosen(player1, true); // play for free
        harness.passBothPriorities(); // resolve the free creature spell

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(MosquitoGuard.class);
    }

    @Test
    @DisplayName("Declining to play leaves the card on top of the library (not exiled)")
    void declineToPlayLeavesCardOnTop() {
        addCreatureReady(player1, new LeafCrownedElder());
        Card top = new LeafCrownedElder();
        setLibraryTop(top);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true); // reveal
        harness.handleMayAbilityChosen(player1, false); // decline to play

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(top);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void decliningRevealLeavesCardInLibrary() {
        addCreatureReady(player1, new LeafCrownedElder());
        Card top = new LeafCrownedElder();
        setLibraryTop(top);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(top);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void emptyLibraryDoesNotOfferReveal() {
        addCreatureReady(player1, new LeafCrownedElder());
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void matchingKindredInstantCanBeCastForFree() {
        addCreatureReady(player1, new LeafCrownedElder());
        Card top = new ReachOfBranches();
        setLibraryTop(top);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(top);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(top);
    }

    @Test
    void triggerUsesLastKnownCreatureTypesAfterElderIsDestroyed() {
        var elder = addCreatureReady(player1, new LeafCrownedElder());
        Card top = new LeafCrownedElder();
        setLibraryTop(top);
        harness.setHand(player2, List.of(new VioletPall()));
        harness.addMana(player2, ManaColor.BLACK, 5);

        advanceToUpkeep(player1);
        harness.castInstant(player2, 0, elder.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(elder);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == top);
    }

    @Test
    void losingCreatureTypesBeforeResolutionPreventsKinshipMatch() {
        var elder = addCreatureReady(player1, new LeafCrownedElder());
        Card top = new LeafCrownedElder();
        setLibraryTop(top);
        harness.setHand(player2, List.of(new NamelessInversion()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        advanceToUpkeep(player1);
        harness.castInstant(player2, 0, elder.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(elder);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(top);
    }

    private void setLibraryTop(Card card) {
        harness.setLibrary(player1, List.of(card, new MosquitoGuard(), new MosquitoGuard(),
                new MosquitoGuard(), new MosquitoGuard()));
    }
}
