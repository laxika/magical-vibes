package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArborealGrazer.class, Forest.class})
class ArborealGrazerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may put a land from hand onto the battlefield tapped")
    void etbPutsLandOntoBattlefieldTapped() {
        Forest forest = new Forest();
        castArborealGrazer(forest);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        Permanent permanent = findPermanent(player1, "Forest");
        assertThat(permanent.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining the ETB leaves the land in hand")
    void decliningLeavesLandInHand() {
        Forest forest = new Forest();
        castArborealGrazer(forest);

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Accepting with no land in hand finishes without putting a nonland onto the battlefield")
    void noLandInHandDoesNothing() {
        harness.setHand(player1, List.of(new ArborealGrazer(), new ArborealGrazer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Arboreal Grazer")).isEqualTo(1);
        harness.assertInHand(player1, "Arboreal Grazer");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ETB puts only one land onto the battlefield")
    void putsOnlyOneLandFromHand() {
        harness.setHand(player1, List.of(new ArborealGrazer(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 1);

        assertThat(countPermanents(player1, "Forest")).isEqualTo(1);
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        harness.assertInHand(player1, "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ETB can put a land onto the battlefield after the normal land play")
    void putsLandAfterNormalLandPlay() {
        harness.setHand(player1, List.of(new Forest(), new ArborealGrazer(), new Forest()));
        harness.playLand(player1, 0);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(countPermanents(player1, "Forest")).isEqualTo(2);
        assertThat(findPermanents(player1, "Forest")).filteredOn(Permanent::isTapped).hasSize(1);
        harness.assertNotInHand(player1, "Forest");
    }

    private void castArborealGrazer(Forest forest) {
        harness.setHand(player1, List.of(new ArborealGrazer(), forest));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

}
