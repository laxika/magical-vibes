package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.s.Shock;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SephirothFabledSoldier.class, SephirothOneWingedAngel.class,
        GrizzlyBears.class, Shock.class})
class SephirothFabledSoldierTest extends BaseCardTest {

    @Test
    void entersAndMaySacrificeAnotherCreatureToDraw() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new SephirothFabledSoldier()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void fourthResolvedDeathTriggerTransformsAndCreatesDeathEmblem() {
        Permanent sephiroth = harness.addToBattlefieldAndReturn(player1, new SephirothFabledSoldier());
        sephiroth.setSummoningSick(false);
        List<Permanent> bears = List.of(
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()),
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()),
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()),
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()),
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()));

        for (int i = 0; i < 4; i++) {
            destroyWithShock(bears.get(i));
        }
        harness.passBothPriorities();

        assertThat(sephiroth.isTransformed())
                .as("resolution counts=%s, player1 life=%s, player2 life=%s, stack=%s, pending=%s",
                        gd.permanentAbilityResolutionsThisTurn,
                        gd.getLife(player1.getId()), gd.getLife(player2.getId()), gd.stack,
                        gd.pendingInteractions)
                .isTrue();
        assertThat(sephiroth.getCard()).isInstanceOf(SephirothOneWingedAngel.class);
        assertThat(gd.emblems).hasSize(1);

        destroyWithShock(bears.get(4));

        harness.assertLife(player2, 15);
        harness.assertLife(player1, 25);
    }

    @Test
    void oneWingedAngelMaySacrificeAnyNumberOfOtherCreaturesToDrawThatMany() {
        SephirothFabledSoldier front = new SephirothFabledSoldier();
        Permanent sephiroth = new Permanent(front);
        sephiroth.setCard(front.getBackFaceCard());
        sephiroth.setTransformed(true);
        sephiroth.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(sephiroth);
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstBear.getId(), secondBear.getId()));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .filteredOn(name -> name.equals("Grizzly Bears"))
                .hasSize(2);
    }

    private void destroyWithShock(Permanent target) {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
    }
}
