package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.h.HengeGuardian;
import com.github.laxika.magicalvibes.cards.w.WildJhovall;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TowerOfTheMagistrate.class, WildJhovall.class, HengeGuardian.class})
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
        Permanent target = addCreatureReady(player1, new WildJhovall());
        Permanent artifactCreature = addCreatureReady(player2, new HengeGuardian());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, target, artifactCreature)).isTrue();
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void grantsProtectionToOpponentsCreature() {
        addReady(new TowerOfTheMagistrate());
        Permanent target = addCreatureReady(player2, new WildJhovall());
        Permanent artifactCreature = addCreatureReady(player1, new HengeGuardian());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, target, artifactCreature)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addReady(new TowerOfTheMagistrate());
        Permanent land = addReady(player2, new TowerOfTheMagistrate());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Protection from artifacts wears off at cleanup")
    void protectionWearsOffAtCleanup() {
        addReady(new TowerOfTheMagistrate());
        Permanent target = addCreatureReady(player1, new WildJhovall());
        Permanent artifactCreature = addCreatureReady(player2, new HengeGuardian());
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
