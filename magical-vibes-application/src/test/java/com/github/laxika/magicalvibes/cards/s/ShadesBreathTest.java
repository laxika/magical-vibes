package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShadesBreath.class, GlorySeeker.class, Conspiracy.class})
class ShadesBreathTest extends BaseCardTest {

    @Test
    @DisplayName("Each creature you control becomes a black Shade until end of turn")
    void transformsOwnCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());

        castShadesBreath();

        assertThat(gqs.hasColor(gd, ownCreature, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasColor(gd, ownCreature, CardColor.WHITE)).isFalse();
        assertThat(gqs.effectiveCreatureSubtypes(gd, ownCreature))
                .containsExactly(CardSubtype.SHADE);
        assertThat(gqs.hasColor(gd, opponentCreature, CardColor.WHITE)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, opponentCreature))
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.SOLDIER);
    }

    @Test
    @DisplayName("Each transformed creature can activate its granted pump ability")
    void grantsPumpAbilityToOwnCreatures() {
        Permanent ownCreature = addCreatureReady(player1, new GlorySeeker());

        castShadesBreath();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Every own creature gets the pump ability, but opposing creatures do not")
    void grantsPumpAbilityToEachOwnCreatureOnly() {
        Permanent firstOwnCreature = addCreatureReady(player1, new GlorySeeker());
        Permanent secondOwnCreature = addCreatureReady(player1, new GlorySeeker());
        Permanent opposingCreature = addCreatureReady(player2, new GlorySeeker());

        castShadesBreath();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, firstOwnCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, firstOwnCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, secondOwnCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, secondOwnCreature)).isEqualTo(3);
        assertThat(gqs.hasColor(gd, opposingCreature, CardColor.WHITE)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, opposingCreature))
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.SOLDIER);

        harness.addMana(player2, ManaColor.BLACK, 1);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creatures entering after resolution are not affected")
    void doesNotAffectCreaturesEnteringLater() {
        addCreatureReady(player1, new GlorySeeker());

        castShadesBreath();

        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());

        assertThat(gqs.hasColor(gd, laterCreature, CardColor.WHITE)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, laterCreature))
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.SOLDIER);
        harness.addMana(player1, ManaColor.BLACK, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The transformation and granted ability wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent ownCreature = addCreatureReady(player1, new GlorySeeker());

        castShadesBreath();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasColor(gd, ownCreature, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasColor(gd, ownCreature, CardColor.BLACK)).isFalse();
        assertThat(gqs.effectiveCreatureSubtypes(gd, ownCreature))
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.SOLDIER);
        assertThatThrownBy(() -> {
            harness.addMana(player1, ManaColor.BLACK, 1);
            harness.activateAbility(player1, 0, 0, null, null);
        }).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The granted ability can be activated repeatedly without tapping")
    void repeatedActivationsStackAndWearOff() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        creature.tap();

        castShadesBreath();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering before resolution receive all effects")
    void affectsCreaturesPresentAtResolution() {
        harness.setHand(player1, List.of(new ShadesBreath()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);

        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        harness.passBothPriorities();

        assertThat(gqs.hasColor(gd, creature, CardColor.BLACK)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, creature)).containsExactly(CardSubtype.SHADE);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("A later Conspiracy replaces the Shade creature type")
    void laterConspiracyOverridesShadeType() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        castShadesBreath();

        harness.castFromHand(player1, new Conspiracy(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        assertThat(gqs.effectiveCreatureSubtypes(gd, creature)).containsExactly(CardSubtype.GOBLIN);
        assertThat(gqs.hasColor(gd, creature, CardColor.BLACK)).isTrue();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    private void castShadesBreath() {
        harness.setHand(player1, List.of(new ShadesBreath()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);
    }
}
