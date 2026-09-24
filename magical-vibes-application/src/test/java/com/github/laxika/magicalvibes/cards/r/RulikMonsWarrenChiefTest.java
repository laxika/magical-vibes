package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RulikMonsWarrenChief.class, Forest.class, GrizzlyBears.class})
class RulikMonsWarrenChiefTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with a land on top offers it onto the battlefield tapped")
    void attackingWithLandOnTopOffersTappedLand() {
        addReadyRulik();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent land = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == forest)
                .findFirst()
                .orElseThrow();
        assertThat(land.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Goblin")).isEmpty();
    }

    @Test
    @DisplayName("Declining the land creates a Goblin and leaves the land on top")
    void decliningLandCreatesGoblin() {
        addReadyRulik();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(forest);
        assertThat(findPermanents(player1, "Goblin")).hasSize(1);
    }

    @Test
    @DisplayName("A nonland top card creates a Goblin without moving the card")
    void nonlandTopCardCreatesGoblin() {
        addReadyRulik();
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
        assertThat(findPermanents(player1, "Goblin")).hasSize(1);
    }

    private void addReadyRulik() {
        addCreatureReady(player1, new RulikMonsWarrenChief());
    }
}
