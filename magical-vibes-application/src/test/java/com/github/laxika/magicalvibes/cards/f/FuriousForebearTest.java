package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FuriousForebearTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1}{W} returns Furious Forebear from the graveyard to hand")
    void payingManaReturnsToHand() {
        FuriousForebear forebear = putForebearInGraveyard();
        destroyCreature(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(forebear);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(forebear);
    }

    @Test
    @DisplayName("Declining to pay leaves Furious Forebear in the graveyard")
    void decliningLeavesItInGraveyard() {
        FuriousForebear forebear = putForebearInGraveyard();
        destroyCreature(player1);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forebear);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forebear);
    }

    @Test
    @DisplayName("A creature an opponent controls dying does not trigger Furious Forebear")
    void opponentCreatureDoesNotTrigger() {
        putForebearInGraveyard();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0,
                harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Furious Forebear dying with another creature does not trigger")
    void dyingWithAnotherCreatureDoesNotTrigger() {
        FuriousForebear forebear = new FuriousForebear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, forebear);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forebear);
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private FuriousForebear putForebearInGraveyard() {
        FuriousForebear forebear = new FuriousForebear();
        harness.setGraveyard(player1, List.of(forebear));
        return forebear;
    }

    private void destroyCreature(Player controller) {
        harness.forceActivePlayer(controller);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(controller, new GrizzlyBears());
        harness.setHand(controller, List.of(new Shock()));
        harness.addMana(controller, ManaColor.RED, 1);

        harness.castInstant(controller, 0,
                harness.getPermanentId(controller, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
