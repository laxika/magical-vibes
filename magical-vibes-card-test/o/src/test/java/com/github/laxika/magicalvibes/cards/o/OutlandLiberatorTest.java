package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.FrenziedTrapbreaker;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OutlandLiberator.class, FrenziedTrapbreaker.class, FountainOfYouth.class, AngelicChorus.class})
class OutlandLiberatorTest extends BaseCardTest {

    @Test
    void frontFaceAbilitySacrificesItselfAndDestroysAnArtifact() {
        Permanent liberator = addCreatureReady(player1, new OutlandLiberator());
        Permanent fountain = addCreatureReady(player2, new FountainOfYouth());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, fountain.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(liberator);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(liberator.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(fountain);
    }

    @Test
    void backFaceAbilitySacrificesItselfAndDestroysAnEnchantment() {
        gd.dayNight = DayNight.NIGHT;
        OutlandLiberator front = new OutlandLiberator();
        Permanent trapbreaker = harness.enterBattlefieldAndReturn(player1, front);
        Permanent chorus = addCreatureReady(player2, new AngelicChorus());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, chorus.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(trapbreaker);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(front);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(chorus);
    }

    @Test
    void transformsToBackFaceWhenNoSpellsWereCastLastTurn() {
        gd.dayNight = DayNight.DAY;
        Permanent liberator = addCreatureReady(player1, new OutlandLiberator());
        gd.spellsCastLastTurn.clear();

        harness.performUntapStep(player1);

        assertThat(liberator.isTransformed()).isTrue();
        assertThat(liberator.getCard()).isInstanceOf(FrenziedTrapbreaker.class);
    }

    @Test
    void transformsBackToFrontFaceWhenTwoSpellsWereCastLastTurn() {
        gd.dayNight = DayNight.DAY;
        Permanent liberator = addCreatureReady(player1, new OutlandLiberator());

        gd.spellsCastLastTurn.clear();
        harness.performUntapStep(player1);
        assertThat(liberator.isTransformed()).isTrue();

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);
        harness.performUntapStep(player2);

        assertThat(liberator.isTransformed()).isFalse();
        assertThat(liberator.getCard()).isInstanceOf(OutlandLiberator.class);
    }

    @Test
    void backFaceAttackTriggerDestroysArtifactDefendingPlayerControls() {
        Permanent trapbreaker = addCreatureReady(player1, new FrenziedTrapbreaker());
        trapbreaker.setTransformed(true);
        Permanent fountain = addCreatureReady(player2, new FountainOfYouth());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, fountain.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(fountain);
    }

    @Test
    void backFaceAttackTriggerCannotTargetAnArtifactControlledByAttacker() {
        addCreatureReady(player1, new FrenziedTrapbreaker()).setTransformed(true);
        Permanent fountain = addCreatureReady(player1, new FountainOfYouth());
        addCreatureReady(player2, new AngelicChorus());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, fountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void enteringBeforeDayOrNightEstablishesDay() {
        gd.dayNight = DayNight.NEITHER;

        Permanent liberator = harness.enterBattlefieldAndReturn(player1, new OutlandLiberator());

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(liberator.isTransformed()).isFalse();
    }

    @Test
    void entersWithBackFaceUpAtNight() {
        gd.dayNight = DayNight.NIGHT;

        Permanent liberator = harness.enterBattlefieldAndReturn(player1, new OutlandLiberator());

        assertThat(liberator.isTransformed()).isTrue();
        assertThat(liberator.getCard()).isInstanceOf(FrenziedTrapbreaker.class);
    }

    @Test
    void nonactivePlayersSpellsDoNotPreventNight() {
        gd.dayNight = DayNight.DAY;
        Permanent liberator = addCreatureReady(player1, new OutlandLiberator());
        gd.previousTurnActivePlayerId = player2.getId();
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 2);

        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(liberator.isTransformed()).isTrue();
    }

    @Test
    void nonactivePlayersSpellsNeitherMakeDayNorCreateAnUpkeepTrigger() {
        gd.dayNight = DayNight.NIGHT;
        Permanent liberator = harness.enterBattlefieldAndReturn(player1, new OutlandLiberator());
        gd.previousTurnActivePlayerId = player2.getId();
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 1);
        gd.spellsCastLastTurn.put(player1.getId(), 2);

        advanceToUpkeep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(liberator.isTransformed()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void frontFaceAbilityCanDestroyAnEnchantmentYouControl() {
        Permanent liberator = addCreatureReady(player1, new OutlandLiberator());
        Permanent chorus = harness.addToBattlefieldAndReturn(player1, new AngelicChorus());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, chorus.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(liberator);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(chorus);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(chorus);
    }

    @Test
    void frontFaceAbilityCannotTargetAnOrdinaryCreature() {
        Permanent liberator = addCreatureReady(player1, new OutlandLiberator());
        Permanent creature = addCreatureReady(player2, new OutlandLiberator());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(liberator);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void backFaceAttackDestroysAnEnchantmentWithoutSacrificingItself() {
        gd.dayNight = DayNight.NIGHT;
        Permanent trapbreaker = harness.enterBattlefieldAndReturn(player1, new OutlandLiberator());
        trapbreaker.setSummoningSick(false);
        Permanent chorus = harness.addToBattlefieldAndReturn(player2, new AngelicChorus());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, chorus.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(chorus);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(trapbreaker);
    }
}
