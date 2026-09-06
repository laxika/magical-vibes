package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MonasteryLoremaster.class, Shock.class, GrizzlyBears.class, Forest.class})
class MonasteryLoremasterTest extends BaseCardTest {

    @Test
    void megamorphReturnsTargetNoncreatureNonlandCardAndPutsOnCounterOnIt() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card spell = new Shock();
        harness.setGraveyard(player1, List.of(creature, land, spell));
        harness.setHand(player1, List.of(new MonasteryLoremaster()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent loremaster = findPermanent(player1, "Monastery Loremaster");
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(loremaster));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(spell.getId());
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        harness.passBothPriorities();

        assertThat(loremaster.isFaceDown()).isFalse();
        assertThat(loremaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).contains(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature, land);
    }

    @Test
    void turningFaceUpWithNoLegalGraveyardTargetDoesNotCreateATargetPrompt() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest()));
        harness.setHand(player1, List.of(new MonasteryLoremaster()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent loremaster = findPermanent(player1, "Monastery Loremaster");
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(loremaster));

        assertThat(loremaster.isFaceDown()).isFalse();
        assertThat(loremaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
