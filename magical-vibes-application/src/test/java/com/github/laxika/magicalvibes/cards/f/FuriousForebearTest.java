package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FuriousForebear.class, GrizzlyBears.class, Shock.class, WrathOfGod.class})
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
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forebear);
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Insufficient mana cannot return Furious Forebear")
    void insufficientManaLeavesItInGraveyard() {
        FuriousForebear forebear = putForebearInGraveyard();
        destroyCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forebear);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forebear);
    }

    @Test
    @DisplayName("Each Forebear in the graveyard triggers independently")
    void multipleForebearsTriggerIndependently() {
        FuriousForebear first = new FuriousForebear();
        FuriousForebear second = new FuriousForebear();
        harness.setGraveyard(player1, List.of(first, second));
        destroyCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second);
    }

    @Test
    @DisplayName("An old trigger cannot return Forebear after it leaves the graveyard and dies again")
    void oldTriggerDoesNotReturnNewGraveyardObject() {
        FuriousForebear forebear = putForebearInGraveyard();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.setGraveyard(player1, List.of());
        harness.addToBattlefield(player1, forebear);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Furious Forebear"));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forebear);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forebear);
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
