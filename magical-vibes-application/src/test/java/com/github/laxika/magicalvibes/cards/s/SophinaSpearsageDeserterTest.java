package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.EsixFractalBloom;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SophinaSpearsageDeserter.class, GrizzlyBears.class, SwordsToPlowshares.class,
        EsixFractalBloom.class})
class SophinaSpearsageDeserterTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with nontoken creatures creates that many Clues")
    void investigatesForEachNontokenAttackingCreature() {
        addCreatureReady(player1, new SophinaSpearsageDeserter());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, createTokenCreature());

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    @Test
    @DisplayName("Sophina investigates for itself when it attacks alone")
    void investigatesForSophinaAlone() {
        addCreatureReady(player1, new SophinaSpearsageDeserter());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void ignoresNonattackingCreaturesOnBothBattlefields() {
        addCreatureReady(player1, new SophinaSpearsageDeserter());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    void doesNotInvestigateWhenOnlyAnotherCreatureAttacks() {
        addCreatureReady(player1, new SophinaSpearsageDeserter());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void triggerSurvivesSophinaRemovalAndCountsRemainingAttackers() {
        Permanent sophina = addCreatureReady(player1, new SophinaSpearsageDeserter());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            harness.castInstant(player1, 0, sophina.getId());
            resolveAllTriggers();
        });

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sophina);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void createsNoClueWhenItsOnlyAttackerLeavesBeforeResolution() {
        Permanent sophina = addCreatureReady(player1, new SophinaSpearsageDeserter());
        harness.setHand(player1, List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.castInstant(player1, 0, sophina.getId());
            resolveAllTriggers();
        });

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sophina);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void createdClueCanBeSacrificedForTwoManaToDraw() {
        addCreatureReady(player1, new SophinaSpearsageDeserter());
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            harness.activateAbility(player1, 1, null, null);
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(drawnCard);
    }

    @Test
    void esixReplacesOnlyTheFirstOfTwoSequentialInvestigations() {
        addCreatureReady(player1, new SophinaSpearsageDeserter());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new EsixFractalBloom());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            harness.handlePermanentChosen(player1, bears.getId());
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Grizzly Bears"))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    private Card createTokenCreature() {
        Card token = new Card();
        token.setName("Test Token");
        token.setType(CardType.CREATURE);
        token.setManaCost("");
        token.setColor(CardColor.GREEN);
        token.setPower(1);
        token.setToughness(1);
        token.setToken(true);
        return token;
    }
}
