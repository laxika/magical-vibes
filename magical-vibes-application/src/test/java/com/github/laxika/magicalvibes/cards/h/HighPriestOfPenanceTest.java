package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HighPriestOfPenanceTest extends BaseCardTest {

    private void shockThePriest() {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "High Priest of Penance"));
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Damage triggers a may-destroy on the chosen nonland permanent")
    void damageDestroysChosenPermanent() {
        harness.addToBattlefield(player1, new HighPriestOfPenance());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        shockThePriest();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the may leaves the chosen permanent alone")
    void decliningDestroysNothing() {
        harness.addToBattlefield(player1, new HighPriestOfPenance());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        shockThePriest();

        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("It can destroy an artifact its own controller controls")
    void canDestroyOwnArtifact() {
        harness.addToBattlefield(player1, new HighPriestOfPenance());
        harness.addToBattlefield(player1, new FountainOfYouth());
        UUID fountainId = harness.getPermanentId(player1, "Fountain of Youth");

        shockThePriest();

        harness.handlePermanentChosen(player1, fountainId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Fountain of Youth");
    }

    @Test
    @DisplayName("A land is not a legal target")
    void landIsNotALegalTarget() {
        harness.addToBattlefield(player1, new HighPriestOfPenance());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID forestId = harness.getPermanentId(player2, "Forest");

        shockThePriest();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).doesNotContain(forestId);
    }
}
