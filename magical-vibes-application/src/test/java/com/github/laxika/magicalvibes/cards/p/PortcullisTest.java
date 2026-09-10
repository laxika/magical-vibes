package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.r.ReinsOfPower;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Portcullis.class, GrizzlyBears.class, LlanowarElves.class, Naturalize.class,
        ReinsOfPower.class, Shock.class})
class PortcullisTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles an entering creature when two other creatures are on the battlefield")
    void exilesEnteringCreatureWithTwoOtherCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        castPortcullis();

        castLlanowarElves();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Llanowar Elves"));
    }

    @Test
    @DisplayName("Counts other creatures across all battlefields")
    void countsCreaturesAcrossBattlefields() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castPortcullis();

        castLlanowarElves();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Llanowar Elves"));
    }

    @Test
    @DisplayName("Does not exile an entering creature when fewer than two other creatures exist")
    void doesNotExileWithFewerThanTwoOtherCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castPortcullis();

        castLlanowarElves();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not create a trigger when the creature enters before the threshold is met")
    void doesNotCreateTriggerBeforeThresholdIsMet() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castPortcullis();

        castLlanowarElves();
        assertThat(gd.stack).isEmpty();

        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Rechecks the creature count when the trigger resolves")
    void rechecksCreatureCountOnResolution() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        castPortcullis();

        castLlanowarElves();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does nothing if the entering creature leaves before its trigger resolves")
    void doesNothingIfEnteringCreatureLeavesBeforeTriggerResolves() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        castPortcullis();

        castLlanowarElves();
        UUID elvesId = harness.getPermanentId(player1, "Llanowar Elves");

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elvesId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> (Object) card.getName())
                .contains("Llanowar Elves");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not track the creature if Portcullis leaves before its trigger resolves")
    void doesNotTrackCreatureIfPortcullisLeavesBeforeTriggerResolves() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        castPortcullis();
        UUID portcullisId = harness.getPermanentId(player1, "Portcullis");

        castLlanowarElves();

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, portcullisId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Portcullis");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Llanowar Elves"));
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    @Test
    @DisplayName("Returns the exiled creature when Portcullis leaves the battlefield")
    void returnsExiledCreatureWhenPortcullisLeaves() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        castPortcullis();

        castLlanowarElves();
        harness.passBothPriorities();

        UUID portcullisId = harness.getPermanentId(player1, "Portcullis");
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, portcullisId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    @Test
    @DisplayName("Returns the exiled creature under its owner's control")
    void returnsExiledCreatureUnderItsOwnersControl() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castPortcullis();

        castLlanowarElves();
        UUID elvesId = harness.getPermanentId(player1, "Llanowar Elves");

        harness.setHand(player1, List.of(new ReinsOfPower()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> (Object) card.getName())
                .contains("Llanowar Elves");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> (Object) card.getName())
                .doesNotContain("Llanowar Elves");

        UUID portcullisId = harness.getPermanentId(player1, "Portcullis");
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, portcullisId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    private void castPortcullis() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new Portcullis(), "{4}");
        harness.passBothPriorities();
    }

    private void castLlanowarElves() {
        harness.castFromHand(player1, new LlanowarElves(), "{G}");
        harness.passBothPriorities();
    }
}
