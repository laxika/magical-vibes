package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.c.CathedralSanctifier;
import com.github.laxika.magicalvibes.cards.s.ScrollOfAvacyn;
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

@CardUsed({UncannySpeed.class, CathedralSanctifier.class, ScrollOfAvacyn.class})
class UncannySpeedTest extends BaseCardTest {

    @Test
    @DisplayName("Grants +3/+0 and haste to the targeted creature")
    void grantsBoostAndHaste() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CathedralSanctifier());
        harness.setHand(player1, List.of(new UncannySpeed()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getPowerModifier()).isEqualTo(3);
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(creature.getGrantedKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("Boost and haste wear off at cleanup step")
    void wearsOffAtCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CathedralSanctifier());
        harness.setHand(player1, List.of(new UncannySpeed()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getGrantedKeywords()).doesNotContain(Keyword.HASTE);
    }

    @Test
    @DisplayName("Can target a creature an opponent controls")
    void canTargetOpponentCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CathedralSanctifier());
        harness.setHand(player1, List.of(new UncannySpeed()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getPowerModifier()).isEqualTo(3);
        assertThat(creature.getGrantedKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new ScrollOfAvacyn());
        harness.setHand(player1, List.of(new UncannySpeed()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player1, "Scroll of Avacyn");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Multiple casts add their power boosts and both expire at cleanup")
    void multipleCastsStackUntilCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CathedralSanctifier());
        harness.setHand(player1, List.of(new UncannySpeed(), new UncannySpeed()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getPowerModifier()).isEqualTo(6);
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Haste lets a summoning-sick creature attack")
    void hasteAllowsAttackingDespiteSummoningSickness() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CathedralSanctifier());
        creature.setSummoningSick(true);
        harness.setHand(player1, List.of(new UncannySpeed()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThat(harness.getAttackLegalityService().canAttack(gd, creature, player1.getId())).isFalse();

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(harness.getAttackLegalityService().canAttack(gd, creature, player1.getId())).isTrue();
        assertThat(creature.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("A removed target does not transfer its effects to another creature")
    void removedTargetMakesSpellFizzle() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CathedralSanctifier());
        harness.setHand(player1, List.of(new UncannySpeed()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new CathedralSanctifier());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Uncanny Speed");
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, other, Keyword.HASTE)).isFalse();
    }
}
