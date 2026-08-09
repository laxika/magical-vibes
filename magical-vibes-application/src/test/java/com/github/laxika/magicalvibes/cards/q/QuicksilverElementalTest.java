package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.o.OgreMenial;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuicksilverElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Gains a target creature's activated ability and pays its red cost with blue mana")
    void gainsTargetCreatureAbilityAndUsesBlueManaAsAnyColor() {
        Permanent quicksilver = addReady(new QuicksilverElemental());
        addReady(new OgreMenial());

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, 0, null, gd.playerBattlefields.get(player1.getId()).get(1).getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, quicksilver)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addReady(new QuicksilverElemental());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Gained activated abilities expire at cleanup")
    void gainedAbilitiesExpireAtCleanup() {
        addReady(new QuicksilverElemental());
        addReady(new OgreMenial());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 0, null, gd.playerBattlefields.get(player1.getId()).get(1).getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        gs.advanceStep(gd);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReady(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
