package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MACH1SwoopingScoundrel.class, AngelOfMercy.class, GrizzlyBears.class})
class MACH1SwoopingScoundrelTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils 1 when it enters")
    void surveilsWhenItEnters() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new MACH1SwoopingScoundrel()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Surveils 1 on the first life gain each turn")
    void surveilsOnFirstLifeGainEachTurn() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new MACH1SwoopingScoundrel());
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("The enter-the-battlefield trigger uses the once-each-turn limit")
    void enterTriggerUsesOnceEachTurnLimit() {
        Card etbTopCard = new GrizzlyBears();
        Card lifeGainTopCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(etbTopCard, lifeGainTopCard));
        harness.setHand(player1, List.of(new MACH1SwoopingScoundrel(), new AngelOfMercy()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).contains(lifeGainTopCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
