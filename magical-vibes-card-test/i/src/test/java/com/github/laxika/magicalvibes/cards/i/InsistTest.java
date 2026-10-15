package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BaskingRootwalla;
import com.github.laxika.magicalvibes.cards.l.Liquify;
import com.github.laxika.magicalvibes.cards.o.ObsessiveSearch;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Insist.class, Liquify.class, BaskingRootwalla.class, ObsessiveSearch.class})
class InsistTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card")
    void drawsACard() {
        Card drawnCard = new BaskingRootwalla();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new Insist()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("The next creature spell can't be countered")
    void nextCreatureSpellCantBeCountered() {
        resolveInsist();

        BaskingRootwalla rootwalla = new BaskingRootwalla();
        harness.setHand(player1, List.of(rootwalla));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new Liquify()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, rootwalla.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Basking Rootwalla");
        harness.assertInGraveyard(player2, "Liquify");
    }

    @Test
    @DisplayName("A noncreature spell does not consume the grant")
    void noncreatureSpellDoesNotConsumeGrant() {
        resolveInsist();

        harness.setLibrary(player1, List.of(new BaskingRootwalla()));
        harness.setHand(player1, List.of(new ObsessiveSearch()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0);

        BaskingRootwalla rootwalla = new BaskingRootwalla();
        harness.setHand(player1, List.of(rootwalla));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new Liquify()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, rootwalla.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Basking Rootwalla");
        harness.assertInGraveyard(player2, "Liquify");
    }

    @Test
    @DisplayName("Only the next creature spell can't be countered")
    void grantAppliesOnlyToNextCreatureSpell() {
        resolveInsist();

        BaskingRootwalla firstRootwalla = new BaskingRootwalla();
        BaskingRootwalla secondRootwalla = new BaskingRootwalla();
        harness.setHand(player1, List.of(firstRootwalla, secondRootwalla));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new Liquify()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, secondRootwalla.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Basking Rootwalla");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(secondRootwalla);
        harness.assertInGraveyard(player2, "Liquify");
    }

    @Test
    @DisplayName("The uncounterable grant expires at end of turn")
    void grantExpiresAtEndOfTurn() {
        resolveInsist();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        BaskingRootwalla rootwalla = new BaskingRootwalla();
        harness.setHand(player1, List.of(rootwalla));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new Liquify()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, rootwalla.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Basking Rootwalla");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(rootwalla);
        harness.assertInGraveyard(player2, "Liquify");
    }

    @Test
    @DisplayName("Countering Insist prevents both the draw and the protection")
    void counteredInsistDoesNotGrantProtectionOrDraw() {
        Insist insist = new Insist();
        BaskingRootwalla draw = new BaskingRootwalla();
        BaskingRootwalla creature = new BaskingRootwalla();
        harness.setLibrary(player1, List.of(draw));
        harness.setHand(player1, List.of(insist, creature));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new Liquify(), new Liquify()));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, insist.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(insist);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        harness.assertNotOnBattlefield(player1, "Basking Rootwalla");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("Two resolved copies protect the same next creature, not two creatures")
    void multipleGrantsApplyToSameNextCreature() {
        resolveInsist();
        resolveInsist();

        BaskingRootwalla first = new BaskingRootwalla();
        BaskingRootwalla second = new BaskingRootwalla();
        harness.setHand(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new Liquify(), new Liquify()));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, first.getId());
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, second.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId()).containsExactly(first.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(second);
    }

    private void resolveInsist() {
        harness.setLibrary(player1, List.of(new BaskingRootwalla()));
        harness.setHand(player1, List.of(new Insist()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
