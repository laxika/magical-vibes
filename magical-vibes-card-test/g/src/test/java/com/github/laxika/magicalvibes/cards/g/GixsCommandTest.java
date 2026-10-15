package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
import com.github.laxika.magicalvibes.cards.c.CradleClearcutter;
import com.github.laxika.magicalvibes.cards.d.Disfigure;
import com.github.laxika.magicalvibes.cards.o.ObstinateBaloth;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GixsCommand.class, ArgothianSprite.class, CradleClearcutter.class, ObstinateBaloth.class, Disfigure.class})
class GixsCommandTest extends BaseCardTest {

    @Test
    void countersAndLifelinkModeAffectsCreatureChosenDuringResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        ArgothianSprite firstGraveyardCreature = new ArgothianSprite();
        CradleClearcutter secondGraveyardCreature = new CradleClearcutter();
        Disfigure noncreature = new Disfigure();
        harness.setGraveyard(player1, List.of(firstGraveyardCreature, secondGraveyardCreature, noncreature));
        harness.setHand(player1, List.of(new GixsCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 2}, List.of());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));
        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
        harness.assertInHand(player1, firstGraveyardCreature.getName());
        harness.assertInHand(player1, secondGraveyardCreature.getName());
        harness.assertInGraveyard(player1, noncreature.getName());

        harness.passUntil(TurnStep.END_STEP);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isFalse();
        assertThat(bears.getEffectivePower()).isEqualTo(4);
    }

    @Test
    void countersAndLifelinkModeCanBeCastWithoutChoosingACreature() {
        harness.addToBattlefield(player2, new ArgothianSprite());
        harness.setHand(player1, List.of(new GixsCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 1}, List.of());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        harness.assertNotOnBattlefield(player2, "Argothian Sprite");
    }

    @Test
    void destroysCreaturesWithPowerTwoOrLess() {
        harness.addToBattlefield(player1, new ArgothianSprite());
        harness.addToBattlefield(player2, new CradleClearcutter());
        harness.addToBattlefield(player2, new ArgothianSprite());
        harness.setHand(player1, List.of(new GixsCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{1, 2}, List.of());
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Argothian Sprite");
        harness.assertNotOnBattlefield(player2, "Argothian Sprite");
        harness.assertOnBattlefield(player2, "Cradle Clearcutter");
    }

    @Test
    void eachOpponentSacrificesTheirGreatestPowerCreature() {
        harness.addToBattlefield(player2, new CradleClearcutter());
        harness.addToBattlefield(player2, new ObstinateBaloth());
        harness.setHand(player1, List.of(new GixsCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{2, 3}, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Obstinate Baloth");
        harness.assertOnBattlefield(player2, "Cradle Clearcutter");
    }

    @Test
    void countersAreAppliedBeforeCheckingWhichCreaturesToDestroy() {
        Permanent saved = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        harness.addToBattlefield(player2, new ArgothianSprite());
        harness.setHand(player1, List.of(new GixsCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 1}, List.of());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(saved.getId()));

        harness.assertOnBattlefield(player1, "Argothian Sprite");
        assertThat(saved.getEffectivePower()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, saved, Keyword.LIFELINK)).isTrue();
        harness.assertInGraveyard(player2, "Argothian Sprite");
    }

    @Test
    void creatureCanBeChosenFromOpponentsBattlefieldAfterCasting() {
        harness.setHand(player1, List.of(new GixsCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 2}, List.of());
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId()));

        assertThat(chosen.getEffectivePower()).isEqualTo(4);
        assertThat(chosen.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, chosen, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void returnModeCanReturnCreatureDestroyedByEarlierMode() {
        harness.addToBattlefield(player1, new ArgothianSprite());
        harness.addToBattlefield(player2, new ArgothianSprite());
        harness.setHand(player1, List.of(new GixsCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{1, 2}, List.of());
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Argothian Sprite");
        harness.assertNotInGraveyard(player1, "Argothian Sprite");
        harness.assertInGraveyard(player2, "Argothian Sprite");
        harness.assertNotInHand(player2, "Argothian Sprite");
    }

    @Test
    void returnModeAllowsReturningOnlyOneCreatureAndContinuesToSacrificeMode() {
        ArgothianSprite returned = new ArgothianSprite();
        CradleClearcutter left = new CradleClearcutter();
        harness.setGraveyard(player1, List.of(returned, left, new Disfigure()));
        harness.addToBattlefield(player2, new ObstinateBaloth());
        harness.setHand(player1, List.of(new GixsCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{2, 3}, List.of());
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleGraveyardCardChosen(player1, -1);

        harness.assertInHand(player1, returned.getName());
        harness.assertInGraveyard(player1, left.getName());
        harness.assertInGraveyard(player1, "Disfigure");
        harness.assertInGraveyard(player2, "Obstinate Baloth");
    }

    @Test
    void returnModeAllowsReturningZeroCreatures() {
        harness.setGraveyard(player1, List.of(new ArgothianSprite()));
        harness.setGraveyard(player2, List.of(new CradleClearcutter()));
        harness.setHand(player1, List.of(new GixsCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{2, 3}, List.of());
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Argothian Sprite");
        harness.assertNotInHand(player1, "Argothian Sprite");
        harness.assertInGraveyard(player2, "Cradle Clearcutter");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentChoosesWhichOfTiedGreatestPowerCreaturesToSacrifice() {
        harness.addToBattlefield(player1, new ObstinateBaloth());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new ObstinateBaloth());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ObstinateBaloth());
        harness.addToBattlefield(player2, new CradleClearcutter());
        harness.setHand(player1, List.of(new GixsCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{2, 3}, List.of());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first).doesNotContain(second);
        harness.assertOnBattlefield(player2, "Cradle Clearcutter");
        harness.assertInGraveyard(player2, "Obstinate Baloth");
        harness.assertOnBattlefield(player1, "Obstinate Baloth");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 5);
    }
}
