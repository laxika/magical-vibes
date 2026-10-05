package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({Kitesail.class, WalkingCorpse.class, Unsummon.class})
class KitesailTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+0 and has flying")
    void equippedCreatureGetsBoostAndFlying() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent kitesail = harness.addToBattlefieldAndReturn(player1, new Kitesail());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();

        kitesail.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Boost and flying are lost when the Kitesail is unattached")
    void effectsLostWhenUnattached() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent kitesail = harness.addToBattlefieldAndReturn(player1, new Kitesail());
        kitesail.setAttachedTo(creature.getId());

        kitesail.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Other creatures are unaffected")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent other = addCreatureReady(player1, new WalkingCorpse());
        Permanent kitesail = harness.addToBattlefieldAndReturn(player1, new Kitesail());
        kitesail.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Equip {2} attaches the Kitesail to target creature")
    void equipAttachesToTargetCreature() {
        Permanent kitesail = harness.addToBattlefieldAndReturn(player1, new Kitesail());
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(kitesail.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
    }

    @Test
    void equipMovesBothBonusesToAnotherCreature() {
        Permanent kitesail = harness.addToBattlefieldAndReturn(player1, new Kitesail());
        Permanent first = addCreatureReady(player1, new WalkingCorpse());
        Permanent second = addCreatureReady(player1, new WalkingCorpse());
        kitesail.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, second.getId());
        assertThat(kitesail.getAttachedTo()).isEqualTo(first.getId());
        harness.passBothPriorities();

        assertThat(kitesail.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isTrue();
    }

    @Test
    void equipCannotTargetOpponentCreature() {
        Permanent kitesail = harness.addToBattlefieldAndReturn(player1, new Kitesail());
        Permanent creature = addCreatureReady(player2, new WalkingCorpse());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(kitesail.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipCannotTargetNoncreature() {
        Permanent kitesail = harness.addToBattlefieldAndReturn(player1, new Kitesail());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new Kitesail());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, other.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(kitesail.getAttachedTo()).isNull();
    }

    @Test
    void equipRequiresTwoMana() {
        Permanent kitesail = harness.addToBattlefieldAndReturn(player1, new Kitesail());
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(kitesail.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipCannotBeActivatedDuringCombat() {
        Permanent kitesail = harness.addToBattlefieldAndReturn(player1, new Kitesail());
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(kitesail.getAttachedTo()).isNull();
    }

    @Test
    void equipCannotBeActivatedWithAnAbilityOnTheStack() {
        Permanent kitesail = harness.addToBattlefieldAndReturn(player1, new Kitesail());
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(kitesail.getAttachedTo()).isNull();
        harness.passBothPriorities();
        assertThat(kitesail.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void bonusesApplyToAttachedCreatureControlledByOpponent() {
        Permanent creature = addCreatureReady(player2, new WalkingCorpse());
        Permanent kitesail = harness.addToBattlefieldAndReturn(player1, new Kitesail());
        kitesail.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    void failedEquipPreservesOriginalAttachmentAndBonuses() {
        Permanent kitesail = harness.addToBattlefieldAndReturn(player1, new Kitesail());
        Permanent first = addCreatureReady(player1, new WalkingCorpse());
        Permanent second = addCreatureReady(player1, new WalkingCorpse());
        kitesail.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, second.getId());

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, second.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Walking Corpse");
        assertThat(kitesail.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
