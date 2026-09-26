package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FeralShadow;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OnduWarCleric;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShadowRitePriest.class, OnduWarCleric.class, FeralShadow.class, GrizzlyBears.class})
class ShadowRitePriestTest extends BaseCardTest {

    @Test
    @DisplayName("Other Clerics you control get +1/+1")
    void boostsOtherClericsYouControl() {
        Permanent cleric = addCreatureReady(player1, new OnduWarCleric());
        int clericPower = gqs.getEffectivePower(gd, cleric);
        int clericToughness = gqs.getEffectiveToughness(gd, cleric);
        Permanent nonCleric = addCreatureReady(player1, new GrizzlyBears());
        int nonClericPower = gqs.getEffectivePower(gd, nonCleric);
        Permanent opponentCleric = addCreatureReady(player2, new OnduWarCleric());
        int opponentClericPower = gqs.getEffectivePower(gd, opponentCleric);
        Permanent priest = addCreatureReady(player1, new ShadowRitePriest());
        int priestPower = gqs.getEffectivePower(gd, priest);
        int priestToughness = gqs.getEffectiveToughness(gd, priest);

        assertThat(gqs.getEffectivePower(gd, cleric)).isEqualTo(clericPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, cleric)).isEqualTo(clericToughness + 1);
        assertThat(gqs.getEffectivePower(gd, priest)).isEqualTo(priestPower);
        assertThat(gqs.getEffectiveToughness(gd, priest)).isEqualTo(priestToughness);
        assertThat(gqs.getEffectivePower(gd, nonCleric)).isEqualTo(nonClericPower);
        assertThat(gqs.getEffectivePower(gd, opponentCleric)).isEqualTo(opponentClericPower);
    }

    @Test
    @DisplayName("Sacrificing another Cleric searches for a black creature and puts it onto the battlefield")
    void sacrificesAnotherClericAndSearchesForBlackCreature() {
        Permanent priest = addCreatureReady(player1, new ShadowRitePriest());
        Permanent cleric = addCreatureReady(player1, new OnduWarCleric());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new FeralShadow()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(priest), null, null);

        assertThat(priest.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Ondu War Cleric");
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(card -> card.getName())
                .containsExactly("Feral Shadow");
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Feral Shadow");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cleric);
    }

    @Test
    @DisplayName("The ability cannot be activated without another Cleric to sacrifice")
    void cannotActivateWithoutAnotherCleric() {
        Permanent priest = addCreatureReady(player1, new ShadowRitePriest());
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(priest), null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
