package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LysAlanaInformant.class, GrizzlyBears.class, Shock.class})
class LysAlanaInformantTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield surveils 1")
    void entersWithSurveil() {
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, topCard);
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();

        harness.setHand(player1, List.of(new LysAlanaInformant()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore + 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("When it dies, Lys Alana Informant surveils 1")
    void diesWithSurveil() {
        Permanent informant = addReadyInformant(player1);
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, topCard);

        killWithShock(player2, informant.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Another creature's death does not trigger Lys Alana Informant")
    void anotherCreatureDeathDoesNotTrigger() {
        addReadyInformant(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        killWithShock(player1, bears.getId());

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering can leave the surveilled card on top without drawing it")
    void enteringCanKeepTopCard() {
        Card topCard = new LysAlanaInformant();
        Card nextCard = new LysAlanaInformant();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setHand(player1, List.of(new LysAlanaInformant()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("A death trigger can leave the surveilled card on top")
    void dyingCanKeepTopCard() {
        Permanent informant = addReadyInformant(player1);
        Card topCard = new LysAlanaInformant();
        harness.setLibrary(player1, List.of(topCard));

        killWithShock(player2, informant.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(informant.getCard()).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Entering with an empty library completes without a choice or a draw")
    void enteringWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new LysAlanaInformant()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanent(player1, "Lys Alana Informant")).isNotNull();
    }

    @Test
    @DisplayName("An opponent's Informant death surveils that opponent's library")
    void opponentInformantSurveilsItsControllersLibrary() {
        Permanent informant = addReadyInformant(player2);
        Card ownTop = new LysAlanaInformant();
        Card opponentTop = new LysAlanaInformant();
        Card opponentNext = new LysAlanaInformant();
        harness.setLibrary(player1, List.of(ownTop));
        harness.setLibrary(player2, List.of(opponentTop, opponentNext));

        killWithShock(player1, informant.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownTop);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentNext);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(informant.getCard(), opponentTop);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(opponentTop, ownTop);
    }

    private Permanent addReadyInformant(Player player) {
        return addCreatureReady(player, new LysAlanaInformant());
    }

    private void killWithShock(Player caster, UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castAndResolveInstant(caster, 0, targetId);
    }
}
