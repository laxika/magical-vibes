package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AmyPond.class, AncestralVision.class, Forest.class})
class AmyPondTest extends BaseCardTest {

    @Test
    void partnerWithRoryLetsTargetPlayerSearchTheirLibrary() {
        Card rory = new Card();
        rory.setName("Rory Williams");
        Forest decoy = new Forest();
        harness.setLibrary(player2, List.of(decoy, rory));
        harness.setHand(player1, List.of(new AmyPond()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).contains(rory);
    }

    @Test
    void combatDamageRemovesThatManyCountersFromOneOwnedSuspendedCard() {
        AmyPond amy = new AmyPond();
        addCreatureReady(player1, amy);
        AncestralVision vision = new AncestralVision();
        harness.setExile(player1, List.of(vision));
        gd.exiledCardTimeCounters.put(vision.getId(), 5);

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.SuspendedCardTimeCounterChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(vision.getId()));

        assertThat(gd.exiledCardTimeCounters).containsEntry(vision.getId(), 3);
    }
}
