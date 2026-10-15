package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
import com.github.laxika.magicalvibes.cards.c.CandlesOfLeng;
import com.github.laxika.magicalvibes.cards.m.MagusOfTheScroll;
import com.github.laxika.magicalvibes.cards.p.PrismaticLens;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuddenSpoiling.class, SerraAvenger.class, BenalishCavalry.class, CandlesOfLeng.class,
        MagusOfTheScroll.class, StonewoodInvocation.class, PrismaticLens.class})
class SuddenSpoilingTest extends BaseCardTest {

    @Test
    @DisplayName("Makes all creatures controlled by the target player 0/2 without abilities")
    void spoilsTargetPlayersCreatures() {
        Permanent targetCreature = harness.addToBattlefieldAndReturn(player2, new SerraAvenger());
        Permanent targetNoncreature = harness.addToBattlefieldAndReturn(player2, new CandlesOfLeng());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new BenalishCavalry());

        castSuddenSpoiling(player2.getId());

        assertThat(targetCreature.getEffectivePower()).isZero();
        assertThat(targetCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, targetCreature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, targetCreature, Keyword.VIGILANCE)).isFalse();
        assertThat(targetNoncreature.getEffectiveToughness()).isZero();
        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLANKING)).isTrue();

        harness.setLibrary(player2, List.of(new BenalishCavalry()));
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.activateAbility(player2, 1, null, null);
        harness.passBothPriorities();
        assertThat(targetNoncreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Effects wear off at end of turn")
    void effectsWearOffAtCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BenalishCavalry());

        castSuddenSpoiling(player2.getId());
        assertThat(creature.getEffectivePower()).isZero();
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a non-player object")
    void cannotTargetNonPlayer() {
        Permanent candles = harness.addToBattlefieldAndReturn(player2, new CandlesOfLeng());
        harness.setHand(player1, List.of(new SuddenSpoiling()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID candlesId = candles.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, candlesId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("This spell can only target players");
    }

    @Test
    @DisplayName("Split second prevents spells and non-mana activated abilities")
    void splitSecondPreventsResponses() {
        Permanent candles = harness.addToBattlefieldAndReturn(player2, new CandlesOfLeng());
        harness.setHand(player1, List.of(new SuddenSpoiling()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new SuddenSpoiling()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 5);

        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(candles.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target its controller without affecting the opponent's creatures")
    void canTargetItsController() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new SerraAvenger());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new BenalishCavalry());

        castSuddenSpoiling(player1.getId());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.FLANKING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering after resolution keep their abilities and stats")
    void laterArrivalsAreUnaffected() {
        Permanent original = harness.addToBattlefieldAndReturn(player2, new SerraAvenger());

        castSuddenSpoiling(player2.getId());
        Permanent laterArrival = harness.enterBattlefieldAndReturn(player2, new SerraAvenger());

        assertThat(gqs.getEffectivePower(gd, original)).isZero();
        assertThat(gqs.hasKeyword(gd, original, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, laterArrival)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, laterArrival)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, laterArrival, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, laterArrival, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Counters still modify the new base power and toughness")
    void countersStillApply() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SerraAvenger());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        castSuddenSpoiling(player2.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Printed activated abilities are unavailable until cleanup")
    void activatedAbilitiesReturnAfterCleanup() {
        Permanent magus = harness.addToBattlefieldAndReturn(player2, new MagusOfTheScroll());
        magus.setSummoningSick(false);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.setHand(player2, List.of(new BenalishCavalry()));

        castSuddenSpoiling(player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(magus.isTapped()).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, magus)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, magus)).isEqualTo(1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.activateAbility(player2, 0, null, player1.getId());
        assertThat(magus.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Earlier shroud is removed, while an earlier power and toughness boost remains")
    void earlierKeywordGrantIsRemovedButBoostRemains() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SerraAvenger());
        harness.setHand(player2, List.of(new StonewoodInvocation()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        castSuddenSpoiling(player2.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(7);
    }

    @Test
    @DisplayName("Abilities granted after resolution survive ability removal")
    void laterKeywordGrantSurvives() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SerraAvenger());

        castSuddenSpoiling(player2.getId());
        harness.setHand(player2, List.of(new StonewoodInvocation()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(7);
    }

    @Test
    @DisplayName("Split second allows mana abilities without resolving the spell")
    void splitSecondAllowsManaAbilities() {
        Permanent lens = harness.addToBattlefieldAndReturn(player2, new PrismaticLens());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SerraAvenger());
        harness.setHand(player1, List.of(new SuddenSpoiling()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, player2.getId());

        harness.activateAbility(player2, 0, null, null);

        assertThat(lens.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isZero();
    }

    private void castSuddenSpoiling(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new SuddenSpoiling()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, targetPlayerId);
    }
}
