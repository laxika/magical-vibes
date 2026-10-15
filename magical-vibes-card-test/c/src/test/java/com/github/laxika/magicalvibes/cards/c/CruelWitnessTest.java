package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FlameBlessedBolt;
import com.github.laxika.magicalvibes.cards.s.SnarlingWolf;
import com.github.laxika.magicalvibes.cards.w.WeddingInvitation;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CruelWitness.class, SnarlingWolf.class, FlameBlessedBolt.class, WeddingInvitation.class})
class CruelWitnessTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils 1 when its controller casts a noncreature spell")
    void surveilsWhenControllerCastsNoncreatureSpell() {
        harness.addToBattlefield(player1, new CruelWitness());
        Card topCard = new SnarlingWolf();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        castFlameBlessedBolt(player1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Declining surveil leaves the top card on the library")
    void decliningSurveilLeavesTopCardOnLibrary() {
        harness.addToBattlefield(player1, new CruelWitness());
        Card topCard = new SnarlingWolf();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        castFlameBlessedBolt(player1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    @DisplayName("Casting a creature spell does not surveil")
    void creatureSpellDoesNotSurveil() {
        harness.addToBattlefield(player1, new CruelWitness());
        Card topCard = new SnarlingWolf();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        harness.setHand(player1, List.of(new SnarlingWolf()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not surveil")
    void opponentNoncreatureSpellDoesNotSurveil() {
        harness.addToBattlefield(player1, new CruelWitness());
        Card topCard = new SnarlingWolf();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        castFlameBlessedBolt(player2);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    @DisplayName("Surveil puts only the top card into the graveyard before the spell resolves")
    void surveilsOnlyOneCardBeforeSpellResolves() {
        harness.addToBattlefield(player1, new CruelWitness());
        Card topCard = new SnarlingWolf();
        Card secondCard = new SnarlingWolf();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        castFlameBlessedBolt(player1);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard).doesNotContain(secondCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Test
    @DisplayName("Surveilling an empty library does not draw a card or lose the game")
    void emptyLibraryDoesNotPreventTriggerResolution() {
        harness.addToBattlefield(player1, new CruelWitness());
        harness.setLibrary(player1, List.of());
        castFlameBlessedBolt(player1);

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Each Cruel Witness triggers independently for the same noncreature spell")
    void multipleWitnessesSurveilSeparately() {
        harness.addToBattlefield(player1, new CruelWitness());
        harness.addToBattlefield(player1, new CruelWitness());
        Card topCard = new SnarlingWolf();
        Card secondCard = new SnarlingWolf();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        castFlameBlessedBolt(player1);

        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard).doesNotContain(secondCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Casting a noncreature permanent spell also triggers surveil")
    void artifactSpellTriggersSurveil() {
        harness.addToBattlefield(player1, new CruelWitness());
        Card topCard = new SnarlingWolf();
        Card secondCard = new SnarlingWolf();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setHand(player1, List.of(new WeddingInvitation()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castArtifact(player1, 0);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Wedding Invitation");
    }

    private void castFlameBlessedBolt(Player player) {
        harness.setHand(player, List.of(new FlameBlessedBolt()));
        harness.addMana(player, ManaColor.RED, 1);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castInstant(player, 0, harness.getPermanentId(player1, "Cruel Witness"));
    }
}
