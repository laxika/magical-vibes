package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SurgeOfBrilliance.class, GrizzlyBears.class})
class SurgeOfBrillianceTest extends BaseCardTest {

    @Test
    @DisplayName("Draws for spells cast from outside the hand, but not hand casts")
    void drawsForOutsideHandSpellsOnly() {
        Card exiledSpell = new GrizzlyBears();
        Card handSpell = new GrizzlyBears();
        Card drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));

        gd.addToExile(player1.getId(), exiledSpell);
        gd.exilePlayPermissions.put(exiledSpell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, exiledSpell.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(handSpell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new SurgeOfBrilliance()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Can be foretold and draws for its own non-hand cast")
    void foretellsAndCastsFromExile() {
        Card drawnCard = new GrizzlyBears();
        SurgeOfBrilliance spell = new SurgeOfBrilliance();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(spell.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }
}
