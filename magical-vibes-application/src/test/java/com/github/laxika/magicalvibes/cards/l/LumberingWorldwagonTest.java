package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LumberingWorldwagonTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of lands you control and toughness stays 4")
    void powerCountsControlledLands() {
        Permanent wagon = addReadyWagon();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Island());

        assertThat(gqs.getEffectivePower(gd, wagon)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wagon)).isEqualTo(4);
    }

    @Test
    @DisplayName("Entering the battlefield may search for a basic land and puts it tapped")
    void entersWithBasicLandSearch() {
        setupLibrary();
        harness.setHand(player1, List.of(new LumberingWorldwagon()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        searchAndChooseLand();
    }

    @Test
    @DisplayName("Attacking may search for a basic land after the Vehicle is crewed")
    void attackingWithWagonTriggersSearch() {
        setupLibrary();
        Permanent wagon = addReadyWagon();
        Permanent firstCrew = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        firstCrew.setSummoningSick(false);
        Permanent secondCrew = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        secondCrew.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(wagon.isTapped()).isTrue();
    }

    private Permanent addReadyWagon() {
        Permanent wagon = harness.addToBattlefieldAndReturn(player1, new LumberingWorldwagon());
        wagon.setSummoningSick(false);
        return wagon;
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Forest(), new Mountain(), new Island(), new GrizzlyBears()));
    }

    private void searchAndChooseLand() {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        String chosenName = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().getFirst().getName();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        Permanent land = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals(chosenName))
                .findFirst()
                .orElseThrow();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
