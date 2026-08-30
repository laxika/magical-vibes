package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpiritOfTheAldergardTest extends BaseCardTest {

    @Test
    @DisplayName("Counts other snow permanents you control")
    void countsOtherSnowPermanentsYouControl() {
        Permanent spirit = new Permanent(new SpiritOfTheAldergard());
        gd.playerBattlefields.get(player1.getId()).add(spirit);
        harness.addToBattlefield(player1, new SnowCoveredForest());
        harness.addToBattlefield(player1, new SnowCoveredIsland());

        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not count an opponent's or nonsnow permanent")
    void doesNotCountOpponentOrNonsnowPermanent() {
        Permanent spirit = new Permanent(new SpiritOfTheAldergard());
        gd.playerBattlefields.get(player1.getId()).add(spirit);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new SnowCoveredForest());

        assertThat(gqs.getEffectivePower(gd, spirit)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(4);
    }

    @Test
    @DisplayName("Enters and searches for a snow land")
    void entersAndSearchesForSnowLand() {
        harness.setHand(player1, List.of(new SpiritOfTheAldergard()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setLibrary(player1, List.of(new SnowCoveredForest(), new Forest(), new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName)
                .containsExactly("Snow-Covered Forest");

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Snow-Covered Forest");
    }
}
