package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FangOfShigeki;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BladeOfTheOni.class, GrizzlyBears.class, FangOfShigeki.class})
class BladeOfTheOniTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsBladeOfTheOnisStaticEffects() {
        Permanent blade = addReadyBlade();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        blade.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, creature)).contains(CardColor.BLACK);
        assertThat(gqs.effectiveCreatureSubtypes(gd, creature)).contains(CardSubtype.DEMON);
        assertThat(gqs.isCreature(gd, blade)).isFalse();
    }

    @Test
    void reconfigureAttachesAndUnattachesTheBlade() {
        Permanent blade = addReadyBlade();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addReconfigureMana();

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.isCreature(gd, blade)).isFalse();

        addReconfigureMana();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isNull();
        assertThat(gqs.isCreature(gd, blade)).isTrue();
    }

    @Test
    void reconfigureCannotTargetAnOpponentsCreature() {
        Permanent blade = addReadyBlade();
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        addReconfigureMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blade.getAttachedTo()).isNull();
    }

    private Permanent addReadyBlade() {
        return addCreatureReady(player1, new BladeOfTheOni());
    }

    @Test
    void reconfigureCannotTargetItself() {
        Permanent blade = addReadyBlade();
        addReconfigureMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, blade.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blade.getAttachedTo()).isNull();
    }

    @Test
    void cannotUnattachWhenNotAttached() {
        addReadyBlade();
        addReconfigureMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothReconfigureAbilitiesAreRestrictedToSorcerySpeed() {
        Permanent blade = addReadyBlade();
        Permanent creature = addCreatureReady(player1, new FangOfShigeki());
        addReconfigureMana();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        blade.setAttachedTo(creature.getId());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blade.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void movingAndUnattachingRestoresHostsAndPreservesTheirOtherCharacteristics() {
        Permanent blade = addReadyBlade();
        Permanent first = addCreatureReady(player1, new FangOfShigeki());
        Permanent second = addCreatureReady(player1, new FangOfShigeki());
        addReconfigureMana();
        harness.activateAbility(player1, 0, 0, null, first.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, first)).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.BLACK);
        assertThat(gqs.effectiveCreatureSubtypes(gd, first))
                .contains(CardSubtype.SNAKE, CardSubtype.NINJA, CardSubtype.DEMON);
        assertThat(gqs.hasKeyword(gd, first, Keyword.DEATHTOUCH)).isTrue();

        addReconfigureMana();
        harness.activateAbility(player1, 0, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, first, Keyword.MENACE)).isFalse();
        assertThat(gqs.getEffectiveColors(gd, first)).containsExactly(CardColor.GREEN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, first)).doesNotContain(CardSubtype.DEMON);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(5);

        addReconfigureMana();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, second, Keyword.MENACE)).isFalse();
        assertThat(gqs.getEffectiveColors(gd, second)).containsExactly(CardColor.GREEN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, second)).doesNotContain(CardSubtype.DEMON);
        assertThat(gqs.isCreature(gd, blade)).isTrue();
    }

    private void addReconfigureMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
