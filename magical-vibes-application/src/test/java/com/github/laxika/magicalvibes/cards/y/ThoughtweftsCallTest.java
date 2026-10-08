package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KithkinBillyrider;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThoughtweftsCall.class, KithkinBillyrider.class, GrizzlyBears.class, Unsummon.class})
class ThoughtweftsCallTest extends BaseCardTest {

    @Test
    void seeksKithkinAndExilesItAtNextEndStepIfStillInHand() {
        Card kithkin = new KithkinBillyrider();
        harness.setLibrary(player1, List.of(kithkin, new GrizzlyBears()));
        castWithMode(0);

        harness.assertInHand(player1, "Kithkin Billyrider");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotInHand(player1, "Kithkin Billyrider");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(kithkin);
    }

    @Test
    void doesNotExileSoughtKithkinAfterItLeavesHand() {
        Card kithkin = new KithkinBillyrider();
        harness.setLibrary(player1, List.of(kithkin));
        castWithMode(0);
        harness.assertInHand(player1, "Kithkin Billyrider");

        gd.playerHands.get(player1.getId()).remove(kithkin);
        harness.setGraveyard(player1, List.of(kithkin));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(kithkin);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(kithkin);
    }

    @Test
    void perpetuallyBoostsCreatureCardsInHand() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(new ThoughtweftsCall(), bears));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent permanent = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(permanent.getEffectivePower()).isEqualTo(3);
        assertThat(permanent.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    void doesNotTriggerWhenSoughtCardIsNoLongerInHand() {
        harness.setLibrary(player1, List.of(new KithkinBillyrider()));
        castWithMode(0);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Kithkin Billyrider");
    }

    @Test
    void doesNotExileSoughtCardThatWasCastAndReturnedToHand() {
        Card kithkin = new KithkinBillyrider();
        harness.setLibrary(player1, List.of(kithkin));
        castWithMode(0);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Kithkin Billyrider"));
        harness.assertInHand(player1, "Kithkin Billyrider");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertInHand(player1, "Kithkin Billyrider");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(kithkin);
    }

    @Test
    void seekingWithoutAMatchLeavesLibraryUnchangedAndCreatesNoTrigger() {
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        castWithMode(0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void boostsStackAndPersistThroughReturningToHandAndRecasting() {
        harness.setHand(player1, List.of(new ThoughtweftsCall(), new ThoughtweftsCall(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        for (int i = 0; i < 2; i++) {
            harness.castModalSorcery(player1, 0, 1, List.of());
            harness.passBothPriorities();
        }
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent permanent = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(permanent.getEffectivePower()).isEqualTo(4);
        assertThat(permanent.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void boostsEveryCreatureInOwnHandButNotOpponentsHandOrLaterCards() {
        harness.setHand(player1, List.of(new ThoughtweftsCall(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        for (int i = 0; i < 2; i++) {
            harness.castCreature(player1, 0);
            harness.passBothPriorities();
        }
        assertThat(gd.playerBattlefields.get(player1.getId())).allSatisfy(permanent -> {
            assertThat(permanent.getEffectivePower()).isEqualTo(3);
            assertThat(permanent.getEffectiveToughness()).isEqualTo(3);
        });

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent later = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(later.getEffectivePower()).isEqualTo(2);
        assertThat(later.getEffectiveToughness()).isEqualTo(2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        Permanent opposing = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(opposing.getEffectivePower()).isEqualTo(2);
        assertThat(opposing.getEffectiveToughness()).isEqualTo(2);
    }

    private void castWithMode(int modeIndex) {
        harness.setHand(player1, List.of(new ThoughtweftsCall()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castModalSorcery(player1, 0, modeIndex, List.of());
        harness.passBothPriorities();
    }
}
