package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.k.KrenkosCommand;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoltenEchoes.class, GrizzlyBears.class, HillGiant.class, KrenkosCommand.class, Clone.class})
class MoltenEchoesTest extends BaseCardTest {

    @Test
    void copiesMatchingNontokenCreatureWithHaste() {
        castEchoes(CardSubtype.BEAR);

        castCreature(new GrizzlyBears(), "{1}{G}");

        List<Permanent> copies = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(copies).hasSize(1);
        assertThat(gqs.hasKeyword(gd, copies.getFirst(), Keyword.HASTE)).isTrue();
    }

    @Test
    void ignoresOtherTypesAndTokenCreatures() {
        castEchoes(CardSubtype.GOBLIN);

        harness.castFromHand(player1, new KrenkosCommand(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Goblin")).hasSize(2);

        castCreature(new HillGiant(), "{3}{R}{R}");
        assertThat(findPermanents(player1, "Hill Giant"))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void copiesExileAtTheBeginningOfNextEndStep() {
        castEchoes(CardSubtype.BEAR);
        castCreature(new GrizzlyBears(), "{1}{G}");

        assertThat(findPermanents(player1, "Grizzly Bears"))
                .filteredOn(p -> p.getCard().isToken()).hasSize(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);

        assertThat(findPermanents(player1, "Grizzly Bears"))
                .filteredOn(p -> p.getCard().isToken()).hasSize(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears"))
                .filteredOn(p -> p.getCard().isToken()).isEmpty();
    }

    @Test
    void stillCopiesCreatureThatLeavesBeforeTriggerResolves() {
        castEchoes(CardSubtype.BEAR);
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        Permanent original = findPermanent(player1, "Grizzly Bears");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, original));

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1)
                .allMatch(p -> p.getCard().isToken());
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void stillCopiesAfterEchoesLeavesBattlefield() {
        castEchoes(CardSubtype.BEAR);
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        Permanent echoes = findPermanent(player1, "Molten Echoes");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, echoes));

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears"))
                .filteredOn(p -> p.getCard().isToken()).hasSize(1);
    }

    @Test
    void ignoresMatchingCreatureEnteringUnderOpponentControl() {
        castEchoes(CardSubtype.BEAR);
        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(findPermanents(player2, "Grizzly Bears")).hasSize(1);
    }

    @Test
    void grantedHasteIsNotCopiedByAnotherCreature() {
        castEchoes(CardSubtype.BEAR);
        castCreature(new GrizzlyBears(), "{1}{G}");
        Permanent token = findPermanents(player1, "Grizzly Bears").stream()
                .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, token.getId());

        Permanent copy = findPermanent(player2, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, copy, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
    }

    private void castEchoes(CardSubtype chosenSubtype) {
        harness.castFromHand(player1, new MoltenEchoes(), "{2}{R}{R}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, chosenSubtype.name());
    }

    private void castCreature(Card creature, String manaCost) {
        harness.castFromHand(player1, creature, manaCost);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
