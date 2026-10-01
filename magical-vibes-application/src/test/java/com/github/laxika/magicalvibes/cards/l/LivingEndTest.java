package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AmrouScout;
import com.github.laxika.magicalvibes.cards.a.AmrouSeekers;
import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LivingEnd.class, AmrouScout.class, AmrouSeekers.class, AshcoatBear.class,
        BenalishCavalry.class, Island.class})
class LivingEndTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Living End with three time counters")
    void suspendExilesWithThreeTimeCounters() {
        LivingEnd card = new LivingEnd();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
    }

    @Test
    @DisplayName("Living End replaces creatures with graveyard creatures and leaves noncreatures")
    void replacesCreaturesFromGraveyards() {
        suspendCard();
        addCreatureReady(player1, new AshcoatBear());
        addCreatureReady(player2, new BenalishCavalry());
        harness.setGraveyard(player1, List.of(new AmrouScout(), new Island()));
        harness.setGraveyard(player2, List.of(new AmrouSeekers(), new Island()));

        for (int i = 0; i < 2; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Amrou Scout");
        harness.assertOnBattlefield(player2, "Amrou Seekers");
        harness.assertInGraveyard(player1, "Ashcoat Bear");
        harness.assertInGraveyard(player2, "Benalish Cavalry");
        harness.assertInGraveyard(player1, "Island");
        harness.assertInGraveyard(player2, "Island");
    }

    private void suspendCard() {
        LivingEnd card = new LivingEnd();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, null);
    }
}
