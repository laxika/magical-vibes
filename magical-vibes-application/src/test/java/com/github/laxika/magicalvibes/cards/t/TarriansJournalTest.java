package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TarriansJournal.class, TheTombOfAclazotz.class, GrizzlyBears.class, Forest.class})
class TarriansJournalTest extends BaseCardTest {

    @Test
    @DisplayName("The front face sacrifices another artifact or creature and draws a card")
    void sacrificesAnotherArtifactOrCreatureAndDraws() {
        Permanent journal = harness.addToBattlefieldAndReturn(player1, new TarriansJournal());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        prepareMainPhase();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears.getCard());
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(journal.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The front face discards the hand and transforms")
    void discardsHandAndTransforms() {
        Permanent journal = harness.addToBattlefieldAndReturn(player1, new TarriansJournal());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase();

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(journal.isTransformed()).isTrue();
        assertThat(journal.getCard()).isInstanceOf(TheTombOfAclazotz.class);
    }

    @Test
    @DisplayName("The back face adds black mana")
    void backFaceAddsBlackMana() {
        addReadyTomb();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("The back face grants one finality counter and Vampire subtype to a graveyard creature cast")
    void graveyardCreatureEntersWithFinalityCounterAndVampireSubtype() {
        addReadyTomb();
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        prepareMainPhase();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof GrizzlyBears)
                .findFirst().orElseThrow();
        assertThat(bears.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, bears)).contains(CardSubtype.VAMPIRE);

        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The back face cannot grant casting permission for a noncreature card")
    void doesNotGrantPermissionForNoncreatureCard() {
        addReadyTomb();
        harness.setGraveyard(player1, List.of(new Forest()));
        prepareMainPhase();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyTomb() {
        TarriansJournal front = new TarriansJournal();
        Permanent tomb = new Permanent(front);
        tomb.setCard(front.getBackFaceCard());
        tomb.setTransformed(true);
        tomb.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(tomb);
        return tomb;
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
