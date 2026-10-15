package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CandyGrapple;
import com.github.laxika.magicalvibes.cards.u.UnassumingSage;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LordSkittersBlessing.class, UnassumingSage.class, Forest.class, CandyGrapple.class})
class LordSkittersBlessingTest extends BaseCardTest {

    @Test
    void entersWithWickedRoleAttachedToTargetCreature() {
        Permanent target = addCreatureReady(player1, new UnassumingSage());
        harness.setHand(player1, List.of(new LordSkittersBlessing()));
        addMana();

        harness.castEnchantment(player1, 0, target.getId());
        resolveAllTriggers();

        Permanent role = findPermanent(player1, "Wicked");
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isFalse();
    }

    @Test
    void losesLifeAndDrawsAdditionalCardAtDrawStepWhileControllingEnchantedCreature() {
        Permanent target = addCreatureReady(player1, new UnassumingSage());
        harness.setHand(player1, List.of(new LordSkittersBlessing()));
        addMana();
        harness.castEnchantment(player1, 0, target.getId());
        resolveAllTriggers();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void doesNotDrawExtraCardOrLoseLifeWithoutAnEnchantedCreature() {
        harness.addToBattlefield(player1, new LordSkittersBlessing());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void doesNotTriggerWithoutAnEnchantedCreatureAtBeginningOfDrawStep() {
        harness.addToBattlefield(player1, new LordSkittersBlessing());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.passUntil(player1, TurnStep.DRAW);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void wickedRoleGoingToGraveyardMakesOnlyOpponentLoseLife() {
        Permanent target = addCreatureReady(player1, new UnassumingSage());
        harness.setHand(player1, List.of(new LordSkittersBlessing()));
        addMana();
        harness.castEnchantment(player1, 0, target.getId());
        resolveAllTriggers();
        harness.setHand(player1, List.of(new CandyGrapple()));
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Wicked");
        harness.assertInGraveyard(player1, "Unassuming Sage");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    void drawAbilityDoesNothingIfLastEnchantedCreatureLeavesBeforeResolution() {
        Permanent target = addCreatureReady(player1, new UnassumingSage());
        harness.setHand(player1, List.of(new LordSkittersBlessing()));
        addMana();
        harness.castEnchantment(player1, 0, target.getId());
        resolveAllTriggers();
        harness.setHand(player1, List.of(new CandyGrapple()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.DRAW);
        assertThat(gd.stack).hasSize(1);
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotTriggerDuringOpponentsDrawStep() {
        Permanent target = addCreatureReady(player1, new UnassumingSage());
        harness.setHand(player1, List.of(new LordSkittersBlessing()));
        addMana();
        harness.castEnchantment(player1, 0, target.getId());
        resolveAllTriggers();
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.forceActivePlayer(player2);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.passUntil(player2, TurnStep.DRAW);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
