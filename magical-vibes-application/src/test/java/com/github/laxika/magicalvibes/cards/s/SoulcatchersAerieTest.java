package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BattleScreech;
import com.github.laxika.magicalvibes.cards.b.BenevolentBodyguard;
import com.github.laxika.magicalvibes.cards.c.CabalTrainee;
import com.github.laxika.magicalvibes.cards.l.LavaDart;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.t.ToxicStench;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({BattleScreech.class, BenevolentBodyguard.class, CabalTrainee.class, LavaDart.class, SoulcatchersAerie.class, SuntailHawk.class, ToxicStench.class})
class SoulcatchersAerieTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a feather counter on itself when a Bird is put into its controller's graveyard")
    void putsFeatherCounterWhenBirdDies() {
        Permanent aerie = harness.addToBattlefieldAndReturn(player1, new SoulcatchersAerie());
        Permanent bird = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());

        killWithLavaDart(player1, bird);

        assertThat(aerie.getCounterCount(CounterType.FEATHER)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for a non-Bird creature")
    void doesNotTriggerForNonBird() {
        Permanent aerie = harness.addToBattlefieldAndReturn(player1, new SoulcatchersAerie());
        Permanent nonBird = harness.addToBattlefieldAndReturn(player1, new CabalTrainee());

        killWithLavaDart(player1, nonBird);

        assertThat(aerie.getCounterCount(CounterType.FEATHER)).isZero();
    }

    @Test
    @DisplayName("Does not trigger for a Bird put into an opponent's graveyard")
    void doesNotTriggerForOpponentsBird() {
        Permanent aerie = harness.addToBattlefieldAndReturn(player1, new SoulcatchersAerie());
        Permanent bird = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());

        killWithLavaDart(player1, bird);

        assertThat(aerie.getCounterCount(CounterType.FEATHER)).isZero();
    }

    @Test
    @DisplayName("Triggers for a Bird owned by its controller even when an opponent controls it")
    void triggersForOwnedBirdUnderOpposingControl() {
        Permanent aerie = harness.addToBattlefieldAndReturn(player1, new SoulcatchersAerie());
        SuntailHawk birdCard = new SuntailHawk();
        birdCard.setOwnerId(player1.getId());
        Permanent bird = harness.addToBattlefieldAndReturn(player2, birdCard);

        killWithLavaDart(player1, bird);

        assertThat(aerie.getCounterCount(CounterType.FEATHER)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Suntail Hawk");
    }

    @Test
    @DisplayName("Does not trigger for an opponent-owned Bird under its controller's control")
    void doesNotTriggerForOpponentOwnedBirdUnderOwnControl() {
        Permanent aerie = harness.addToBattlefieldAndReturn(player1, new SoulcatchersAerie());
        SuntailHawk birdCard = new SuntailHawk();
        birdCard.setOwnerId(player2.getId());
        Permanent bird = harness.addToBattlefieldAndReturn(player1, birdCard);

        killWithLavaDart(player1, bird);

        assertThat(aerie.getCounterCount(CounterType.FEATHER)).isZero();
        harness.assertInGraveyard(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("Bird creatures get +1/+1 for each feather counter")
    void boostsBirdsByFeatherCounterCount() {
        Permanent aerie = harness.addToBattlefieldAndReturn(player1, new SoulcatchersAerie());
        Permanent bird = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        aerie.setCounterCount(CounterType.FEATHER, 2);

        assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bird)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bird creatures controlled by either player get the counter-scaled boost")
    void boostsBirdsRegardlessOfController() {
        Permanent aerie = harness.addToBattlefieldAndReturn(player1, new SoulcatchersAerie());
        Permanent ownBird = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        Permanent opposingBird = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        Permanent nonBird = harness.addToBattlefieldAndReturn(player1, new CabalTrainee());
        aerie.setCounterCount(CounterType.FEATHER, 2);

        assertThat(gqs.getEffectivePower(gd, ownBird)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBird)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingBird)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposingBird)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, nonBird)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nonBird)).isEqualTo(1);
    }

    @Test
    @DisplayName("A Bird token dying also puts a feather counter on the enchantment")
    void putsFeatherCounterWhenBirdTokenDies() {
        Permanent aerie = harness.addToBattlefieldAndReturn(player1, new SoulcatchersAerie());
        harness.setHand(player1, List.of(new BattleScreech()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent birdToken = findPermanents(player1, "Bird").getFirst();
        killWithLavaDart(player1, birdToken);

        assertThat(aerie.getCounterCount(CounterType.FEATHER)).isEqualTo(1);
    }

    private void killWithLavaDart(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new LavaDart()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castAndResolveInstant(caster, 0, target.getId());
        resolveAllTriggers();
    }

    @Test
    @DisplayName("The bonus applies to Birds controlled by either player, but not other creatures")
    void boostsBirdsOnBothBattlefieldsButNotNonBirds() {
        Permanent aerie = harness.addToBattlefieldAndReturn(player1, new SoulcatchersAerie());
        aerie.setCounterCount(CounterType.FEATHER, 2);
        Permanent opponentBird = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        Permanent nonBird = harness.addToBattlefieldAndReturn(player1, new BenevolentBodyguard());

        assertThat(gqs.getEffectivePower(gd, opponentBird)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentBird)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, nonBird)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nonBird)).isEqualTo(1);
    }

    @Test
    @DisplayName("Puts a feather counter on itself when a Bird token is put into its controller's graveyard")
    void putsFeatherCounterWhenBirdTokenDiesJudReview() {
        Permanent aerie = harness.addToBattlefieldAndReturn(player1, new SoulcatchersAerie());

        harness.castFromHand(player1, new BattleScreech(), "{2}{W}{W}");
        harness.passBothPriorities();

        List<Permanent> birds = findPermanents(player1, "Bird");
        assertThat(birds).hasSize(2);

        killWithToxicStenchForJudReview(player1, birds.get(0));

        assertThat(aerie.getCounterCount(CounterType.FEATHER)).isEqualTo(1);
    }

    private void killWithToxicStenchForJudReview(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new ToxicStench()));
        harness.addMana(caster, ManaColor.BLACK, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(caster, 0, target.getId());
        harness.passBothPriorities();
    }
}
