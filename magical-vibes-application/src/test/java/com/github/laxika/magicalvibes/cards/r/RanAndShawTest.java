package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirbendingLesson;
import com.github.laxika.magicalvibes.cards.d.DragonEgg;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RanAndShaw.class, AirbendingLesson.class, DragonEgg.class, GrizzlyBears.class})
class RanAndShawTest extends BaseCardTest {

    @Test
    void castCreatesNonlegendaryTokenCopyWithThreeDragonOrLessonCards() {
        harness.setGraveyard(player1, List.of(
                new AirbendingLesson(), new AirbendingLesson(), new DragonEgg()));
        harness.setHand(player1, List.of(new RanAndShaw()));
        addCastMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> permanents = gd.playerBattlefields.get(player1.getId());
        assertThat(permanents).hasSize(2);
        assertThat(permanents.stream().filter(permanent -> permanent.getCard().isToken())).hasSize(1);
        assertThat(permanents.stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow().getCard().getSupertypes())
                .doesNotContain(CardSupertype.LEGENDARY);
    }

    @Test
    void castDoesNotCreateTokenCopyWithFewerThanThreeDragonOrLessonCards() {
        harness.setGraveyard(player1, List.of(new AirbendingLesson(), new AirbendingLesson()));
        harness.setHand(player1, List.of(new RanAndShaw()));
        addCastMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void enteringWithoutBeingCastDoesNotCreateTokenCopy() {
        harness.setGraveyard(player1, List.of(
                new AirbendingLesson(), new AirbendingLesson(), new DragonEgg()));
        harness.enterBattlefieldAndReturn(player1, new RanAndShaw());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void activatedAbilityBoostsOnlyDragonsUntilEndOfTurn() {
        Permanent ranAndShaw = addCreatureReady(player1, new RanAndShaw());
        Permanent dragon = addCreatureReady(player1, new DragonEgg());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        int ranAndShawPower = gqs.getEffectivePower(gd, ranAndShaw);
        int dragonPower = gqs.getEffectivePower(gd, dragon);
        int bearPower = gqs.getEffectivePower(gd, bear);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ranAndShaw)).isEqualTo(ranAndShawPower + 2);
        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(dragonPower + 2);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(bearPower);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ranAndShaw)).isEqualTo(ranAndShawPower);
    }

    @Test
    void enteringWithoutBeingCastDoesNotTriggerAtAll() {
        harness.setGraveyard(player1, List.of(
                new AirbendingLesson(), new AirbendingLesson(), new AirbendingLesson()));

        harness.enterBattlefieldAndReturn(player1, new RanAndShaw());

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copyConditionIsCheckedAgainWhenTriggerResolves() {
        harness.setGraveyard(player1, List.of(
                new AirbendingLesson(), new AirbendingLesson(), new AirbendingLesson()));
        harness.setHand(player1, List.of(new RanAndShaw()));
        addCastMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.setGraveyard(player1, List.of(new AirbendingLesson(), new AirbendingLesson()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void opponentsGraveyardDoesNotSatisfyCopyCondition() {
        harness.setGraveyard(player2, List.of(
                new AirbendingLesson(), new AirbendingLesson(), new AirbendingLesson()));
        harness.setHand(player1, List.of(new RanAndShaw()));
        addCastMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void threeDragonCardsSatisfyCopyCondition() {
        harness.setGraveyard(player1, List.of(
                new RanAndShaw(), new RanAndShaw(), new RanAndShaw()));
        harness.setHand(player1, List.of(new RanAndShaw()));
        addCastMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    void firebendingAddsTwoRedManaThroughCombatOnly() {
        addCreatureReady(player1, new RanAndShaw());

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void tokenCopyCanActivateDragonBoostAndFirebend() {
        harness.setGraveyard(player1, List.of(
                new AirbendingLesson(), new AirbendingLesson(), new AirbendingLesson()));
        harness.setHand(player1, List.of(new RanAndShaw()));
        addCastMana();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> dragons = gd.playerBattlefields.get(player1.getId());
        Permanent original = dragons.getFirst();
        Permanent token = dragons.stream().filter(p -> p.getCard().isToken()).findFirst().orElseThrow();
        int originalPower = gqs.getEffectivePower(gd, original);
        int tokenPower = gqs.getEffectivePower(gd, token);
        harness.addMana(player1, ManaColor.RED, 4);
        harness.activateAbility(player1, dragons.indexOf(token), null, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(originalPower + 2);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(tokenPower + 2);

        token.setSummoningSick(false);
        declareAttackers(List.of(dragons.indexOf(token)));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    void boostExcludesOpposingDragonsAndLaterEntrants() {
        Permanent source = addCreatureReady(player1, new RanAndShaw());
        Permanent opposingDragon = addCreatureReady(player2, new RanAndShaw());
        int opposingPower = gqs.getEffectivePower(gd, opposingDragon);
        int sourcePower = gqs.getEffectivePower(gd, source);
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        Permanent laterDragon = addCreatureReady(player1, new DragonEgg());

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(sourcePower + 2);
        assertThat(gqs.getEffectivePower(gd, opposingDragon)).isEqualTo(opposingPower);
        assertThat(gqs.getEffectivePower(gd, laterDragon)).isZero();
    }

    private void addCastMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
