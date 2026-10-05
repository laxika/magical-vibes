package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BloodMoon;
import com.github.laxika.magicalvibes.cards.c.CrystalGrotto;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NishobaBrawler;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PixieIllusionist.class, Forest.class, NishobaBrawler.class, CrystalGrotto.class, BloodMoon.class})
class PixieIllusionistTest extends BaseCardTest {

    @Test
    void castWithoutKickerEntersWithoutCounters() {
        harness.setHand(player1, List.of(new PixieIllusionist()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPixie().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void castWithKickerEntersWithTwoCounters() {
        harness.setHand(player1, List.of(new PixieIllusionist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPixie().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void tappedAbilityMakesControlledLandTheChosenBasicTypeUntilEndOfTurn() {
        addCreatureReady(player1, new PixieIllusionist());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);
        UUID forestId = forest.getId();

        harness.activateAbility(player1, 0, null, forestId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ISLAND");

        assertThat(forest.getTransientLandTypeOverride()).isEqualTo(CardSubtype.ISLAND);
        assertThat(forest.getTransientSubtypes()).isEmpty();
    }

    @Test
    void abilityCannotTargetOpponentLand() {
        addCreatureReady(player1, new PixieIllusionist());
        harness.addToBattlefield(player1, new Forest());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.forceActivePlayer(player1);
        UUID opponentForestId = opponentForest.getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentForestId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land you control");
    }

    @Test
    void abilityCannotTargetNonLandPermanent() {
        addCreatureReady(player1, new PixieIllusionist());
        harness.addToBattlefield(player1, new Forest());
        Permanent brawler = harness.addToBattlefieldAndReturn(player1, new NishobaBrawler());
        harness.forceActivePlayer(player1);
        UUID brawlerId = brawler.getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, brawlerId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    void chosenTypeReplacesOriginalManaAndExpiresAtEndOfTurn() {
        Permanent pixie = addCreatureReady(player1, new PixieIllusionist());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, forest.getId());
        assertThat(pixie.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ISLAND");

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.ISLAND);
        harness.tapPermanent(player1, 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.FOREST);
        forest.setTapped(false);
        harness.tapPermanent(player1, 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    void summoningSicknessPreventsPayingTapCost() {
        harness.addToBattlefield(player1, new PixieIllusionist());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPixie().isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityStillResolvesAfterSourceLeavesBattlefield() {
        Permanent pixie = addCreatureReady(player1, new PixieIllusionist());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, 0, null, forest.getId());
        gd.playerBattlefields.get(player1.getId()).remove(pixie);
        gd.playerGraveyards.get(player1.getId()).add(pixie.getCard());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "SWAMP");

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.SWAMP);
    }

    @Test
    void abilityDoesNotChangeLandThatChangesControllerBeforeResolution() {
        addCreatureReady(player1, new PixieIllusionist());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, 0, null, forest.getId());
        gd.playerBattlefields.get(player1.getId()).remove(forest);
        gd.playerBattlefields.get(player2.getId()).add(forest);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.FOREST);
    }

    @Test
    void laterBloodMoonOverridesChosenLandType() {
        addCreatureReady(player1, new PixieIllusionist());
        Permanent grotto = harness.addToBattlefieldAndReturn(player1, new CrystalGrotto());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, grotto.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ISLAND");
        assertThat(gqs.effectiveBasicLandTypes(gd, grotto)).containsExactly(CardSubtype.ISLAND);

        harness.setHand(player1, List.of(new BloodMoon()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, grotto)).containsExactly(CardSubtype.MOUNTAIN);
        harness.tapPermanent(player1, 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    private Permanent findPixie() {
        return findPermanent(player1, "Pixie Illusionist");
    }
}
