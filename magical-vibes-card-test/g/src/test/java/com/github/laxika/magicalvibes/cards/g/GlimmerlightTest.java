package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Glimmerlight.class, GrizzlyBears.class})
class GlimmerlightTest extends BaseCardTest {

    @Test
    void enteringCreatesAGlimmerEnchantmentCreatureToken() {
        harness.setHand(player1, List.of(new Glimmerlight()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent glimmer = findPermanent(player1, "Glimmer");
        assertThat(glimmer.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(glimmer.getCard().getSubtypes()).containsExactly(CardSubtype.GLIMMER);
        assertThat(glimmer.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(glimmer.getCard().hasType(CardType.ENCHANTMENT)).isTrue();
        assertThat(glimmer.getEffectivePower()).isEqualTo(1);
        assertThat(glimmer.getEffectiveToughness()).isEqualTo(1);
        assertThat(glimmer.getCard().isToken()).isTrue();
    }

    @Test
    void equippedCreatureGetsPlusOnePlusOne() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent glimmerlight = addCreatureReady(player1, new Glimmerlight());
        glimmerlight.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void equipAbilityAttachesGlimmerlightToAControlledCreature() {
        Permanent glimmerlight = addCreatureReady(player1, new Glimmerlight());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(glimmerlight.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void canEquipTheTokenCreatedByItsEnterTrigger() {
        harness.setHand(player1, List.of(new Glimmerlight()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent glimmerlight = findPermanent(player1, "Glimmerlight");
        Permanent glimmer = findPermanent(player1, "Glimmer");
        assertThat(countPermanents(player1, "Glimmer")).isEqualTo(1);
        assertThat(glimmerlight.getAttachedTo()).isNull();
        assertThat(gqs.getEffectivePower(gd, glimmer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, glimmer)).isEqualTo(1);

        harness.activateAbility(player1, 0, null, glimmer.getId());
        harness.passBothPriorities();

        assertThat(glimmerlight.getAttachedTo()).isEqualTo(glimmer.getId());
        assertThat(gqs.getEffectivePower(gd, glimmer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, glimmer)).isEqualTo(2);
        assertThat(countPermanents(player1, "Glimmer")).isEqualTo(1);
    }

    @Test
    void reequippingMovesTheBonusToTheNewCreature() {
        Permanent glimmerlight = addCreatureReady(player1, new Glimmerlight());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(glimmerlight.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }

    @Test
    void equipCannotTargetAnOpponentsCreature() {
        Permanent glimmerlight = addCreatureReady(player1, new Glimmerlight());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(glimmerlight.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipCannotBeActivatedOutsideAMainPhase() {
        Permanent glimmerlight = addCreatureReady(player1, new Glimmerlight());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(glimmerlight.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipCannotBeActivatedWhileAnotherEquipIsOnTheStack() {
        Permanent glimmerlight = addCreatureReady(player1, new Glimmerlight());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(glimmerlight.getAttachedTo()).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(glimmerlight.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void equipCannotTargetANoncreatureArtifact() {
        Permanent glimmerlight = addCreatureReady(player1, new Glimmerlight());
        Permanent otherEquipment = addCreatureReady(player1, new Glimmerlight());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, otherEquipment.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(glimmerlight.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
