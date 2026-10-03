package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImperiousMindbreaker.class, GrizzlyBears.class})
class ImperiousMindbreakerTest extends BaseCardTest {

    private Permanent castAndPairWithBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ImperiousMindbreaker()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        return bears;
    }

    @Test
    @DisplayName("The paired creature mills each opponent by its toughness when it attacks")
    void pairedCreatureMillsByItsToughness() {
        Permanent bears = castAndPairWithBears();
        int sizeBefore = gd.playerDecks.get(player2.getId()).size();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bears)));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(sizeBefore - 2);
    }

    @Test
    @DisplayName("Imperious Mindbreaker mills by its own toughness when it attacks")
    void mindbreakerMillsByItsToughness() {
        castAndPairWithBears();
        Permanent mindbreaker = findPermanent(player1, "Imperious Mindbreaker");
        mindbreaker.setSummoningSick(false);
        int sizeBefore = gd.playerDecks.get(player2.getId()).size();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(mindbreaker)));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(sizeBefore - 4);
    }

    @Test
    @DisplayName("An unpaired Imperious Mindbreaker does not grant an attack mill trigger")
    void unpairedDoesNotMill() {
        addCreatureReady(player1, new ImperiousMindbreaker());
        int sizeBefore = gd.playerDecks.get(player2.getId()).size();

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(sizeBefore);
    }
}
