package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.cards.s.SugarCoat;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VrenTheRelentless.class, DoomBlade.class, GrizzlyBears.class,
        SwordsToPlowshares.class, WrathOfGod.class})
class VrenTheRelentlessTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles an opponent's creature instead of letting it die")
    void exilesOpponentCreatureInsteadOfDying() {
        harness.addToBattlefield(player1, new VrenTheRelentless());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Counts opponent creatures exiled this turn, including tokens, and makes other Rats larger")
    void createsRatTokensBasedOnCreaturesExiledThisTurn() {
        harness.addToBattlefield(player1, new VrenTheRelentless());
        Permanent opponentBearsOne = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opponentBearsTwo = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opponentToken = addTokenCreature(player2);
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(
                new SwordsToPlowshares(), new SwordsToPlowshares(),
                new SwordsToPlowshares(), new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        for (var target : List.of(opponentBearsOne, opponentBearsTwo, opponentToken, ownBears)) {
            harness.castAndResolveInstant(player1, 0, target.getId());
        }

        advanceToEndStep(player2);
        harness.passBothPriorities();

        List<Permanent> rats = findPermanents(player1, "Rat");
        assertThat(rats).hasSize(3);
        assertThat(rats).allSatisfy(rat -> {
            assertThat(gqs.getEffectivePower(gd, rat)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, rat)).isEqualTo(4);
        });
    }

    @Test
    @DisplayName("Still exiles an opponent's creature when Vren leaves in the same destruction event")
    void replacementAppliesWhenVrenLeavesAtTheSameTime() {
        harness.addToBattlefield(player1, new VrenTheRelentless());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player2, 0, 0);

        harness.assertNotOnBattlefield(player1, "Vren, the Relentless");
        harness.assertInGraveyard(player1, "Vren, the Relentless");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getName().equals("Grizzly Bears"));
    }

    @Test
    void wardCountersOpponentSpellWhenTheyCannotPay() {
        Permanent vren = harness.addToBattlefieldAndReturn(player1, new VrenTheRelentless());
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player2, 0, vren.getId());

        harness.assertOnBattlefield(player1, "Vren, the Relentless");
        harness.assertInGraveyard(player2, "Swords to Plowshares");
        harness.assertLife(player1, 20);
    }

    @Test
    void payingWardAllowsOpponentSpellToResolve() {
        Permanent vren = harness.addToBattlefieldAndReturn(player1, new VrenTheRelentless());
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castInstant(player2, 0, vren.getId());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vren, the Relentless");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(vren.getCard().getId()));
        harness.assertLife(player1, 23);
    }

    @Test
    @CardUsed({SugarCoat.class})
    void losingAbilitiesStopsDeathReplacement() {
        Permanent vren = harness.addToBattlefieldAndReturn(player1, new VrenTheRelentless());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SugarCoat(), new DoomBlade()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, vren.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(bears.getCard().getId()));
    }

    @Test
    void ownCreaturesStillDieNormallyAndDoNotMakeRats() {
        harness.addToBattlefield(player1, new VrenTheRelentless());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Rat")).isEmpty();
    }

    @Test
    void countsCreaturesExiledBeforeVrenEntered() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.addToBattlefield(player1, new VrenTheRelentless());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Rat")).hasSize(1);
    }

    @Test
    void replacementExilesMakeRatsButDoNotCarryOverToNextTurn() {
        harness.addToBattlefield(player1, new VrenTheRelentless());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());

        advanceToEndStep(player1);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Rat")).hasSize(1);

        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Rat")).hasSize(1);
    }

    @Test
    void countsExilesAtResolutionAndTokensKeepTheirAbilityAfterVrenLeaves() {
        Permanent vren = harness.addToBattlefieldAndReturn(player1, new VrenTheRelentless());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SwordsToPlowshares(), new SwordsToPlowshares()));
        advanceToEndStep(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        List<Permanent> rats = findPermanents(player1, "Rat");
        assertThat(rats).hasSize(1);
        Permanent rat = rats.getFirst();
        assertThat(gqs.getEffectivePower(gd, rat)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rat)).isEqualTo(2);

        harness.castAndResolveInstant(player1, 0, vren.getId());

        assertThat(gqs.getEffectivePower(gd, rat)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rat)).isEqualTo(1);
        harness.addToBattlefield(player1, new VrenTheRelentless());
        assertThat(gqs.getEffectivePower(gd, rat)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rat)).isEqualTo(2);
    }

    private Permanent addTokenCreature(Player owner) {
        Card tokenCard = new Card();
        tokenCard.setName("Bear Token");
        tokenCard.setOwnerId(owner.getId());
        tokenCard.setType(CardType.CREATURE);
        tokenCard.setManaCost("");
        tokenCard.setToken(true);
        tokenCard.setColor(CardColor.GREEN);
        tokenCard.setPower(2);
        tokenCard.setToughness(2);
        tokenCard.setSubtypes(List.of(CardSubtype.BEAR));
        return harness.addToBattlefieldAndReturn(owner, tokenCard);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
