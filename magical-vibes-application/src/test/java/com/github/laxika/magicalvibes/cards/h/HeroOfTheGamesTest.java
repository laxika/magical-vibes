package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SternDismissal;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeroOfTheGames.class, GiantGrowth.class, GrizzlyBears.class, SternDismissal.class})
class HeroOfTheGamesTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts your creatures when you cast a spell targeting Hero of the Games")
    void boostsYourCreaturesWhenTargetedByOwnSpell() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfTheGames());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castGiantGrowth(hero);

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger when your spell targets another creature")
    void doesNotTriggerWhenAnotherCreatureIsTargeted() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfTheGames());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, bears.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(2);
    }

    @Test
    @DisplayName("Hero of the Games's bonus wears off at end of turn")
    void bonusWearsOffAtEndOfTurn() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfTheGames());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castGiantGrowth(hero);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    private void castGiantGrowth(Permanent target) {
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();
    }

    @Test
    @DisplayName("An opponent's spell targeting the Hero does not trigger its ability")
    void opponentSpellDoesNotTrigger() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfTheGames());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new HeroOfTheGames());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);
        harness.castAndResolveInstant(player2, 0, hero.getId());

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The bonus resolves before the targeting spell and excludes opposing creatures")
    void bonusResolvesBeforeSpellAndOnlyAffectsYourCreatures() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfTheGames());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new HeroOfTheGames());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HeroOfTheGames());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, hero.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(3);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(7);
    }

    @Test
    @DisplayName("Each targeting spell gives another bonus during the same turn")
    void bonusesFromSeparateSpellsAccumulate() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfTheGames());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new HeroOfTheGames());

        castGiantGrowth(hero);
        castGiantGrowth(hero);

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(11);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(8);
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering after the bonus resolves do not receive it")
    void laterCreaturesDoNotReceiveResolvedBonus() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfTheGames());
        castGiantGrowth(hero);

        Permanent newcomer = harness.addToBattlefieldAndReturn(player1, new HeroOfTheGames());

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, newcomer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, newcomer)).isEqualTo(2);
    }

    @Test
    @DisplayName("The bonus still resolves when the Hero leaves before its trigger resolves")
    void bonusResolvesAfterHeroLeavesBattlefield() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfTheGames());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new HeroOfTheGames());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, hero.getId());

        harness.setHand(player2, List.of(new SternDismissal()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, hero.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(hero);
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(3);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(2);
        harness.assertInHand(player1, "Hero of the Games");
        harness.assertInGraveyard(player1, "Giant Growth");
    }
}
