package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TowerOfTheMagistrateTest extends BaseCardTest {

    @Test
    @DisplayName("Can tap for colorless mana")
    void tapsForColorlessMana() {
        Permanent tower = addReady(new TowerOfTheMagistrate());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(tower.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Target creature gains protection from artifacts until end of turn")
    void grantsProtectionFromArtifacts() {
        addReady(new TowerOfTheMagistrate());
        Permanent target = addReady(new GrizzlyBears());
        Permanent artifactCreature = addReady(player2, new IronMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, target, artifactCreature)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addReady(new TowerOfTheMagistrate());
        Permanent land = addReady(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Protection from artifacts wears off at cleanup")
    void protectionWearsOffAtCleanup() {
        addReady(new TowerOfTheMagistrate());
        Permanent target = addReady(new GrizzlyBears());
        Permanent artifactCreature = addReady(player2, new IronMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, target, artifactCreature)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, target, artifactCreature)).isFalse();
    }

    private Permanent addReady(Card card) {
        return addReady(player1, card);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
