package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReverberatingSummons.class, LightningBolt.class, Plains.class})
class ReverberatingSummonsTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes a 3/3 Monk creature with haste after casting two spells in a turn")
    void animatesAtBeginningOfCombatAfterTwoSpells() {
        Permanent summons = addSummons();
        prepareMainPhase();
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        advanceToCombat();
        harness.passBothPriorities();

        assertThat(summons.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, summons)).isTrue();
        assertThat(gqs.getEffectivePower(gd, summons)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, summons)).isEqualTo(3);
        assertThat(summons.getTransientSubtypes()).contains(CardSubtype.MONK);
        assertThat(gqs.hasKeyword(gd, summons, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not animate at beginning of combat after only one spell")
    void doesNotAnimateAfterOneSpell() {
        Permanent summons = addSummons();
        prepareMainPhase();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        advanceToCombat();

        assertThat(summons.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, summons)).isFalse();
    }

    @Test
    @DisplayName("Discards the hand, sacrifices itself, and draws two cards")
    void activatesDrawAbility() {
        Permanent summons = addSummons();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.setLibrary(player1, List.of(new Plains(), new Plains()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(summons);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(summons.getCard());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void animatesDuringOpponentsCombatAfterControllerCastsTwoSpells() {
        Permanent summons = addSummons();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        advanceToCombat();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, summons)).isTrue();
        assertThat(gqs.getEffectivePower(gd, summons)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, summons, Keyword.HASTE)).isTrue();
    }

    @Test
    void opponentsSpellsDoNotSatisfyCondition() {
        Permanent summons = addSummons();
        prepareMainPhase();
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());
        advanceToCombat();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, summons)).isFalse();
    }

    @Test
    void castingSecondSpellAfterCombatBeginsDoesNotCreateTrigger() {
        Permanent summons = addSummons();
        prepareMainPhase();
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        advanceToCombat();

        assertThat(gd.stack).isEmpty();
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gqs.isCreature(gd, summons)).isFalse();
    }

    @Test
    void emptyHandCanPayDiscardHandCostAndCostsArePaidBeforeResolution() {
        Permanent summons = addSummons();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Plains(), new Plains()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(summons);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(summons.getCard());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void animationExpiresAtEndOfTurn() {
        Permanent summons = addSummons();
        prepareMainPhase();
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        advanceToCombat();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, summons)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, summons)).isFalse();
        assertThat(gqs.hasKeyword(gd, summons, Keyword.HASTE)).isFalse();
        assertThat(summons.getTransientSubtypes()).doesNotContain(CardSubtype.MONK);
    }

    @Test
    void entireHandIsDiscardedBeforeDrawAbilityResolves() {
        Permanent summons = addSummons();
        LightningBolt first = new LightningBolt();
        LightningBolt second = new LightningBolt();
        harness.setHand(player1, List.of(first, second));
        harness.setLibrary(player1, List.of(new Plains(), new Plains()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(first, second, summons.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(summons);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2).doesNotContain(first, second);
    }

    private Permanent addSummons() {
        return harness.addToBattlefieldAndReturn(player1, new ReverberatingSummons());
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void advanceToCombat() {
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
