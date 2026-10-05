package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.e.EnormousBaloth;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MistformWakecaster.class, EnormousBaloth.class, AmoeboidChangeling.class})
class MistformWakecasterTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability changes Mistform Wakecaster to the chosen type")
    void changesSelfToChosenType() {
        Permanent wakecaster = addWakecaster();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(wakecaster.isTapped()).isFalse();
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        assertThat(gqs.effectiveCreatureSubtypes(gd, wakecaster)).containsExactly(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("The second ability changes each creature you control and not an opponent's creature")
    void changesControlledCreaturesOnly() {
        Permanent wakecaster = addWakecaster();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new EnormousBaloth());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new EnormousBaloth());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(wakecaster.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.WALL.name());

        assertThat(gqs.effectiveCreatureSubtypes(gd, wakecaster)).containsExactly(CardSubtype.WALL);
        assertThat(gqs.effectiveCreatureSubtypes(gd, ownCreature)).containsExactly(CardSubtype.WALL);
        assertThat(gqs.effectiveCreatureSubtypes(gd, opposingCreature)).containsExactly(CardSubtype.BEAST);
    }

    @Test
    @DisplayName("The second ability does not affect creatures that enter after it resolves")
    void doesNotAffectCreaturesEnteringLater() {
        Permanent wakecaster = addWakecaster();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new EnormousBaloth());

        assertThat(gqs.effectiveCreatureSubtypes(gd, wakecaster)).containsExactly(CardSubtype.GOBLIN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, laterCreature)).containsExactly(CardSubtype.BEAST);
    }

    @Test
    @DisplayName("The creature type change wears off at end of turn")
    void changesWearOffAtEndOfTurn() {
        Permanent wakecaster = addWakecaster();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new EnormousBaloth());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.effectiveCreatureSubtypes(gd, wakecaster)).containsExactly(CardSubtype.ILLUSION);
        assertThat(gqs.effectiveCreatureSubtypes(gd, ownCreature)).containsExactly(CardSubtype.BEAST);
    }

    @Test
    void selfAbilityWorksWhileTappedAndSummoningSick() {
        Permanent wakecaster = harness.addToBattlefieldAndReturn(player1, new MistformWakecaster());
        wakecaster.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.WALL.name());

        assertThat(wakecaster.isTapped()).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, wakecaster)).containsExactly(CardSubtype.WALL);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.effectiveCreatureSubtypes(gd, wakecaster)).containsExactly(CardSubtype.ILLUSION);
    }

    @Test
    void massAbilityCannotBeActivatedWhileSummoningSick() {
        harness.addToBattlefieldAndReturn(player1, new MistformWakecaster());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void massAbilityAffectsCreaturesEnteringBeforeResolution() {
        addWakecaster();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new EnormousBaloth());
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        assertThat(gqs.effectiveCreatureSubtypes(gd, creature)).containsExactly(CardSubtype.GOBLIN);
    }

    @Test
    void laterSelfActivationReplacesPreviousChosenType() {
        Permanent wakecaster = addWakecaster();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.WALL.name());

        assertThat(gqs.effectiveCreatureSubtypes(gd, wakecaster)).containsExactly(CardSubtype.WALL);
    }

    @Test
    void laterTypeLossOverridesSelfTypeChange() {
        Permanent wakecaster = addWakecaster();
        addCreatureReady(player2, new AmoeboidChangeling());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());
        harness.activateAbility(player2, 0, 1, null, wakecaster.getId());
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, wakecaster)).isEmpty();
    }

    @Test
    void laterTypeLossOverridesMassTypeChange() {
        Permanent wakecaster = addWakecaster();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new EnormousBaloth());
        addCreatureReady(player2, new AmoeboidChangeling());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());
        harness.activateAbility(player2, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, creature)).isEmpty();
        assertThat(gqs.effectiveCreatureSubtypes(gd, wakecaster)).containsExactly(CardSubtype.GOBLIN);
    }

    private Permanent addWakecaster() {
        Permanent wakecaster = addCreatureReady(player1, new MistformWakecaster());
        harness.forceActivePlayer(player1);
        return wakecaster;
    }
}
