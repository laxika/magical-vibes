package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.c.CerebralDownload;
import com.github.laxika.magicalvibes.cards.i.IslandSanctuary;
import com.github.laxika.magicalvibes.cards.t.ThoughtReflection;
import com.github.laxika.magicalvibes.cards.x.XuIfitOsteoharmonist;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QuantumRiddler.class, CerebralDownload.class, ThoughtReflection.class,
        IslandSanctuary.class, XuIfitOsteoharmonist.class})
class QuantumRiddlerTest extends BaseCardTest {

    @Test
    void skippedDrawsDoNotReapplyTheWholeInstructionReplacement() {
        harness.addToBattlefield(player1, new QuantumRiddler());
        harness.addToBattlefield(player1, new IslandSanctuary());
        harness.setHand(player1, List.of(new CerebralDownload()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        addCerebralDownloadMana();
        harness.castAndResolveInstant(player1, 0);

        for (int i = 0; i < 4; i++) {
            assertThat(gd.interaction.isAwaitingInput()).isTrue();
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.pendingCardDraws).isEmpty();
    }

    @Test
    void entersAndDrawsAnAdditionalCardWithOneOrFewerCardsInHand() {
        harness.setLibrary(player1, List.of(
                new CerebralDownload(), new CerebralDownload()));
        harness.setHand(player1, List.of(new QuantumRiddler()));
        addQuantumRiddlerMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void addsOnlyOneCardToAWholeMultiCardDrawInstruction() {
        harness.addToBattlefield(player1, new QuantumRiddler());
        harness.setLibrary(player1, List.of(
                new CerebralDownload(), new CerebralDownload(), new CerebralDownload(), new CerebralDownload()));
        harness.setHand(player1, List.of(new CerebralDownload()));
        addCerebralDownloadMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotReplaceDrawsWhenTheControllerHasMoreThanOneCardInHand() {
        harness.addToBattlefield(player1, new QuantumRiddler());
        harness.setLibrary(player1, List.of(new CerebralDownload(), new CerebralDownload()));
        harness.setHand(player1, List.of(new CerebralDownload(), new CerebralDownload()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void quantumRiddlerAndThoughtReflectionApplyInSequence() {
        harness.addToBattlefield(player1, new QuantumRiddler());
        harness.addToBattlefield(player1, new ThoughtReflection());
        harness.setLibrary(player1, List.of(
                new CerebralDownload(), new CerebralDownload(), new CerebralDownload(), new CerebralDownload(),
                new CerebralDownload(), new CerebralDownload(), new CerebralDownload(), new CerebralDownload()));
        harness.setHand(player1, List.of(new CerebralDownload()));
        addCerebralDownloadMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(8);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void canBeCastForItsWarpCost() {
        harness.setLibrary(player1, List.of(new CerebralDownload(), new CerebralDownload()));
        harness.setHand(player1, List.of(new QuantumRiddler()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof QuantumRiddler);
    }

    @Test
    void eachRiddlerAddsOneCardToTheSameDrawInstruction() {
        harness.addToBattlefield(player1, new QuantumRiddler());
        harness.addToBattlefield(player1, new QuantumRiddler());
        harness.setLibrary(player1, List.of(new CerebralDownload(), new CerebralDownload(),
                new CerebralDownload(), new CerebralDownload(), new CerebralDownload()));
        harness.setHand(player1, List.of(new CerebralDownload()));
        addCerebralDownloadMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotReplaceDrawsAfterReturningWithoutAbilities() {
        QuantumRiddler riddler = new QuantumRiddler();
        harness.addToBattlefieldAndReturn(player1, new XuIfitOsteoharmonist()).setSummoningSick(false);
        harness.setGraveyard(player1, List.of(riddler));
        harness.setHand(player1, List.of(new CerebralDownload()));
        harness.setLibrary(player1, List.of(new CerebralDownload(), new CerebralDownload(),
                new CerebralDownload(), new CerebralDownload()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, riddler.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Quantum Riddler");
        assertThat(gd.stack).isEmpty();
        addCerebralDownloadMana();
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void replacesDrawsWithExactlyOneCardInHand() {
        harness.addToBattlefield(player1, new QuantumRiddler());
        harness.setHand(player1, List.of(new CerebralDownload(), new CerebralDownload()));
        harness.setLibrary(player1, List.of(new CerebralDownload(), new CerebralDownload(),
                new CerebralDownload(), new CerebralDownload()));
        addCerebralDownloadMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotReplaceOpponentsDraws() {
        harness.addToBattlefield(player1, new QuantumRiddler());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new CerebralDownload()));
        harness.setLibrary(player2, List.of(new CerebralDownload(), new CerebralDownload(),
                new CerebralDownload(), new CerebralDownload()));
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void warpExilesAtEndStepAndAllowsNormalCastingOnALaterTurn() {
        QuantumRiddler riddler = new QuantumRiddler();
        harness.setHand(player1, List.of(riddler));
        harness.setLibrary(player1, List.of(new CerebralDownload(), new CerebralDownload(),
                new CerebralDownload(), new CerebralDownload(), new CerebralDownload(),
                new CerebralDownload()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.passUntil(TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Quantum Riddler");
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Quantum Riddler");
        assertThat(gd.findExiledCard(riddler.getId())).isNotNull();

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        int handSize = gd.playerHands.get(player1.getId()).size();
        addQuantumRiddlerMana();
        harness.castFromExile(player1, riddler.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
        assertThat(gd.findExiledCard(riddler.getId())).isNull();
        harness.passUntil(TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Quantum Riddler");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void normalCastDrawsOnlyOneWithTwoCardsRemainingAndDoesNotExile() {
        QuantumRiddler riddler = new QuantumRiddler();
        harness.setHand(player1, List.of(riddler, new CerebralDownload(), new CerebralDownload()));
        harness.setLibrary(player1, List.of(new CerebralDownload(), new CerebralDownload()));
        addQuantumRiddlerMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.passUntil(TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Quantum Riddler");
        assertThat(gd.findExiledCard(riddler.getId())).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void addQuantumRiddlerMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void addCerebralDownloadMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
