package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NissaVastwoodSeerTest extends BaseCardTest {

    @Test
    @DisplayName("The enter trigger offers only basic Forest cards and puts the chosen one into hand")
    void enterTriggerFetchesBasicForest() {
        setLibrary(new Forest(), new Mountain(), new GrizzlyBears());
        castNissa();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .hasSize(1)
                .allMatch(c -> c.getName().equals("Forest"));

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Declining the enter trigger leaves the library untouched")
    void decliningEnterTriggerFetchesNothing() {
        setLibrary(new Forest(), new Mountain());
        castNissa();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A land entering does not transform Nissa while you control fewer than seven lands")
    void landfallBelowSevenLandsDoesNotTransform() {
        setLibrary(new GrizzlyBears());
        Permanent nissa = addReadyNissa(player1);
        addLands(player1, 5);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nissa, Vastwood Seer");
        harness.assertNotOnBattlefield(player1, "Nissa, Sage Animist");
        assertThat(nissa.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("The seventh land counts itself, so Nissa returns transformed as a planeswalker")
    void landfallAtSevenLandsTransforms() {
        setLibrary(new GrizzlyBears());
        addReadyNissa(player1);
        addLands(player1, 6);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nissa, Vastwood Seer");
        harness.assertOnBattlefield(player1, "Nissa, Sage Animist");

        Permanent walker = findPermanent(player1, "Nissa, Sage Animist");
        assertThat(walker.isTransformed()).isTrue();
        assertThat(walker.getCard().hasType(CardType.PLANESWALKER)).isTrue();
        assertThat(walker.getCounterCount(CounterType.LOYALTY)).isPositive();
    }

    @Test
    @DisplayName("A land an opponent plays never transforms Nissa")
    void opponentLandDoesNotTransform() {
        setLibrary(new GrizzlyBears());
        Permanent nissa = addReadyNissa(player1);
        addLands(player1, 6);
        addLands(player2, 6);
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nissa, Vastwood Seer");
        assertThat(nissa.isTransformed()).isFalse();
    }

    private void castNissa() {
        harness.setHand(player1, List.of(new NissaVastwoodSeer()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addReadyNissa(Player player) {
        Permanent perm = new Permanent(new NissaVastwoodSeer());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private void addLands(Player player, int count) {
        for (int i = 0; i < count; i++) {
            gd.playerBattlefields.get(player.getId()).add(new Permanent(new Forest()));
        }
    }

    private void setLibrary(Card... cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(new ArrayList<>(List.of(cards)));
    }
}
