package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.c.CerebralDownload;
import com.github.laxika.magicalvibes.cards.t.ThoughtReflection;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QuantumRiddler.class, CerebralDownload.class, ThoughtReflection.class})
class QuantumRiddlerTest extends BaseCardTest {

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

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

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

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

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

    private void addQuantumRiddlerMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void addCerebralDownloadMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
