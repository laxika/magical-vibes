package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DrownedRusalka.class, GrizzlyBears.class, Forest.class, Island.class})
class DrownedRusalkaTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature, then discards and draws a card")
    void sacrificesThenDiscardsAndDraws() {
        Permanent rusalka = addCreatureReady(player1, new DrownedRusalka());
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        Forest discarded = new Forest();
        Island drawn = new Island();
        harness.setHand(player1, new ArrayList<>(List.of(discarded)));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(rusalka).doesNotContain(fodder);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Can sacrifice itself as the creature cost")
    void canSacrificeItself() {
        Permanent rusalka = addCreatureReady(player1, new DrownedRusalka());
        Forest discarded = new Forest();
        Island drawn = new Island();
        harness.setHand(player1, new ArrayList<>(List.of(discarded)));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Drowned Rusalka");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }
}
