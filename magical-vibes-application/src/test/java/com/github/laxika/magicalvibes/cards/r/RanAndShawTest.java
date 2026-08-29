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
        harness.passBothPriorities();
        harness.passBothPriorities();

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
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void enteringWithoutBeingCastDoesNotCreateTokenCopy() {
        harness.setGraveyard(player1, List.of(
                new AirbendingLesson(), new AirbendingLesson(), new DragonEgg()));
        harness.addToBattlefield(player1, new RanAndShaw());
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

    private void addCastMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
