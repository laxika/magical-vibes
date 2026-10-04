package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({EssenceknitScholar.class, Forest.class})
class EssenceknitScholarTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Essenceknit Scholar creates a 1/1 Pest token")
    void etbCreatesPestToken() {
        harness.setHand(player1, List.of(new EssenceknitScholar()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent pest = findPermanent(player1, "Pest");
        assertThat(countPermanents(player1, "Pest")).isEqualTo(1);
        assertThat(pest.getCard().isToken()).isTrue();
        assertThat(pest.getCard().getPower()).isEqualTo(1);
        assertThat(pest.getCard().getToughness()).isEqualTo(1);
        assertThat(pest.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
        assertThat(pest.getCard().getSubtypes()).contains(CardSubtype.PEST);
    }

    @Test
    void pestAttackGainsLifeEvenIfPestDiesBeforeResolution() {
        harness.enterBattlefieldAndReturn(player1, new EssenceknitScholar());
        resolveAllTriggers();
        Permanent pest = findPermanent(player1, "Pest");
        pest.setSummoningSick(false);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(pest))));
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, pest));
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    void noTriggerWithoutCreatureDeath() {
        harness.addToBattlefield(player1, new EssenceknitScholar());
        advanceToEndStep(player1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void pestDeathCountsForEndStepDraw() {
        harness.enterBattlefieldAndReturn(player1, new EssenceknitScholar());
        resolveAllTriggers();
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent pest = findPermanent(player1, "Pest");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, pest));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertLife(player1, 20);
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new EssenceknitScholar());
        gd.creatureDeathCountThisTurn.put(player1.getId(), 1);
        advanceToEndStep(player2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void multipleCreatureDeathsDrawOnlyOneCard() {
        harness.addToBattlefield(player1, new EssenceknitScholar());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        gd.creatureDeathCountThisTurn.put(player1.getId(), 3);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draws a card if a creature died under your control this turn")
    void drawsWhenCreatureDiedUnderYourControl() {
        harness.addToBattlefield(player1, new EssenceknitScholar());
        harness.setLibrary(player1, List.of(new Forest()));
        gd.creatureDeathCountThisTurn.put(player1.getId(), 1);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player1);
        // Trigger is on the stack; resolve it
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Does not draw when only an opponent's creature died this turn")
    void noDrawWhenOnlyOpponentCreatureDied() {
        harness.addToBattlefield(player1, new EssenceknitScholar());
        harness.setLibrary(player1, List.of(new Forest()));
        gd.creatureDeathCountThisTurn.put(player2.getId(), 1); // opponent's death only

        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player, TurnStep.END_STEP);
    }

}
