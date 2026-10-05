package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaliciousMalfunction.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class MaliciousMalfunctionTest extends BaseCardTest {

    @Test
    @DisplayName("Gives all creatures -2/-2 and exiles creatures that die this turn")
    void weakensAllCreaturesAndExilesThoseThatDie() {
        Permanent ownSurvivor = addCreatureReady(player1, new HillGiant());
        Permanent opponentSurvivor = addCreatureReady(player2, new HillGiant());
        Card ownDying = new GrizzlyBears();
        Card opponentDying = new GrizzlyBears();
        addCreatureReady(player1, ownDying);
        addCreatureReady(player2, opponentDying);
        harness.setHand(player1, List.of(new MaliciousMalfunction()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gqs.getEffectivePower(gd, ownSurvivor)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownSurvivor)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentSurvivor)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentSurvivor)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ownDying);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opponentDying);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(ownDying);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(opponentDying);
    }

    @Test
    @DisplayName("The debuff and replacement effect expire at end of turn")
    void effectsExpireAtEndOfTurn() {
        Card giantCard = new HillGiant();
        addCreatureReady(player2, giantCard);
        harness.setHand(player1, List.of(new MaliciousMalfunction()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        Permanent giant = findPermanent(player2, "Hill Giant");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, giant));

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(giantCard);
    }

    @Test
    @DisplayName("A surviving creature killed later this turn is exiled")
    void exilesSurvivorKilledLaterThisTurn() {
        Card giantCard = new HillGiant();
        Permanent giant = addCreatureReady(player2, giantCard);
        harness.setHand(player1, List.of(new MaliciousMalfunction(), new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.castAndResolveInstant(player1, 0, giant.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(giant);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(giantCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(giantCard);
    }

    @Test
    @DisplayName("Creatures entering later avoid the debuff but are still exiled if they die")
    void replacementAlsoAppliesToCreaturesEnteringLater() {
        harness.setHand(player1, List.of(new MaliciousMalfunction(), new GrizzlyBears(), new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, 0);

        Card bearCard = gd.playerHands.get(player1.getId()).getFirst();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bear);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bearCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bearCard);
    }

    @Test
    @DisplayName("Surviving creatures recover their power and toughness after cleanup")
    void debuffExpiresAfterCleanup() {
        Permanent giant = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new MaliciousMalfunction()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }
}
