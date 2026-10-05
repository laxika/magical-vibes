package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.j.JoinTheDance;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.w.WitnessProtection;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QuandrixTheProof.class, QuickStudy.class, Divination.class, HillGiant.class,
        JoinTheDance.class, LlanowarElves.class, WitnessProtection.class})
class QuandrixTheProofTest extends BaseCardTest {

    @Test
    @DisplayName("Quandrix, the Proof has cascade when cast")
    void ownCascadeTriggersWhenCast() {
        setupWithQuandrix();
        harness.setLibrary(player1, List.of(new LlanowarElves()));

        harness.setHand(player1, List.of(new QuandrixTheProof()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
    }

    @Test
    @DisplayName("Instant spells cast from hand get cascade")
    void instantFromHandGetsCascade() {
        setupWithQuandrix();
        harness.setLibrary(player1, List.of(new LlanowarElves()));

        harness.setHand(player1, List.of(new QuickStudy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
    }

    @Test
    @DisplayName("Sorcery spells cast from hand get cascade")
    void sorceryFromHandGetsCascade() {
        setupWithQuandrix();
        harness.setLibrary(player1, List.of(new LlanowarElves()));

        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
    }

    @Test
    @DisplayName("Creature spells do not get cascade")
    void creatureDoesNotGetCascade() {
        setupWithQuandrix();
        harness.setLibrary(player1, List.of(new LlanowarElves()));

        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Spells cast from the graveyard do not get cascade")
    void graveyardSpellDoesNotGetCascade() {
        setupWithQuandrix();
        harness.setLibrary(player1, List.of(new LlanowarElves()));

        harness.setGraveyard(player1, List.of(new JoinTheDance()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    void abilityRemovingAuraStopsGrantingCascade() {
        setupWithQuandrix();
        harness.setHand(player1, List.of(new WitnessProtection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0,
                harness.getPermanentId(player1, "Quandrix, the Proof"));
        harness.passBothPriorities();

        harness.setLibrary(player1, List.of(new LlanowarElves(), new HillGiant(), new HillGiant()));
        harness.setHand(player1, List.of(new QuickStudy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertInHand(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Quick Study");
    }

    @Test
    void opponentDoesNotReceiveCascade() {
        setupWithQuandrix();
        harness.setLibrary(player2, List.of(new LlanowarElves(), new HillGiant(), new HillGiant()));
        harness.setHand(player2, List.of(new QuickStudy()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertInHand(player2, "Llanowar Elves");
    }

    @Test
    void grantedCascadeUsesCastSpellsManaValueAndCastsHitForFree() {
        setupWithQuandrix();
        HillGiant skipped = new HillGiant();
        LlanowarElves hit = new LlanowarElves();
        HillGiant untouched = new HillGiant();
        harness.setLibrary(player1, List.of(skipped, hit, untouched));
        harness.setHand(player1, List.of(new QuickStudy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, skipped);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    void ownCascadeCanBeDeclinedAndReturnsHitToBottom() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        LlanowarElves hit = new LlanowarElves();
        HillGiant untouched = new HillGiant();
        harness.setLibrary(player1, List.of(hit, untouched));
        harness.setHand(player1, List.of(new QuandrixTheProof()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, hit);
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Quandrix, the Proof");
    }

    @Test
    void cascadeSkipsEqualManaValueAndReturnsAllCardsWhenThereIsNoHit() {
        setupWithQuandrix();
        QuickStudy equalManaValue = new QuickStudy();
        HillGiant higherManaValue = new HillGiant();
        harness.setLibrary(player1, List.of(equalManaValue, higherManaValue));
        harness.setHand(player1, List.of(new QuickStudy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(equalManaValue, higherManaValue);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void instantOrSorceryCastByCascadeDoesNotReceiveGrantedCascade() {
        setupWithQuandrix();
        Divination hit = new Divination();
        LlanowarElves untouched = new LlanowarElves();
        harness.setLibrary(player1, List.of(hit, untouched));
        harness.setHand(player1, List.of(new QuandrixTheProof()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
        assertThat(gd.stack).hasSize(2);
    }

    private void setupWithQuandrix() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new QuandrixTheProof());
    }
}
