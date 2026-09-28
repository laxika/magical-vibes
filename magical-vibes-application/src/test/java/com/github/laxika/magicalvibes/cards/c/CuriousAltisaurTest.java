package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CuriousAltisaur.class, RaptorCompanion.class, GrizzlyBears.class, Forest.class})
class CuriousAltisaurTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when a Dinosaur you control deals combat damage to a player")
    void drawsWhenDinosaurDealsCombatDamage() {
        addCuriousAltisaur();
        Permanent dinosaur = addCreatureReady(player1, new RaptorCompanion());
        dinosaur.setAttacking(true);
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when a non-Dinosaur creature deals combat damage")
    void ignoresNonDinosaurCombatDamage() {
        addCuriousAltisaur();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Draws once for each Dinosaur that deals combat damage")
    void drawsForEachDinosaur() {
        addCuriousAltisaur();
        Permanent firstDinosaur = addCreatureReady(player1, new RaptorCompanion());
        firstDinosaur.setAttacking(true);
        Permanent secondDinosaur = addCreatureReady(player1, new RaptorCompanion());
        secondDinosaur.setAttacking(true);
        Card firstTopCard = new Forest();
        Card secondTopCard = new Forest();
        harness.setLibrary(player1, List.of(firstTopCard, secondTopCard));
        harness.setHand(player1, List.of());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstTopCard, secondTopCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private Permanent addCuriousAltisaur() {
        return harness.addToBattlefieldAndReturn(player1, new CuriousAltisaur());
    }
}
