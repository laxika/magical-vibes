package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StoneSeederHierophant.class, Forest.class, BorosRecruit.class})
class StoneSeederHierophantTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall untaps Stone-Seeder Hierophant")
    void landfallUntapsSelf() {
        Permanent hierophant = harness.addToBattlefieldAndReturn(player1, new StoneSeederHierophant());
        hierophant.setSummoningSick(false);
        hierophant.tap();
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(hierophant.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's land does not trigger landfall")
    void opponentsLandDoesNotTriggerLandfall() {
        Permanent hierophant = harness.addToBattlefieldAndReturn(player1, new StoneSeederHierophant());
        hierophant.tap();
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player2, 0);

        assertThat(hierophant.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The activated ability untaps target land")
    void activatedAbilityUntapsTargetLand() {
        Permanent hierophant = harness.addToBattlefieldAndReturn(player1, new StoneSeederHierophant());
        hierophant.setSummoningSick(false);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.tap();

        harness.activateAbility(player1, 0, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The activated ability taps Stone-Seeder Hierophant as a cost")
    void activatedAbilityTapsSource() {
        Permanent hierophant = harness.addToBattlefieldAndReturn(player1, new StoneSeederHierophant());
        hierophant.setSummoningSick(false);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.tap();

        harness.activateAbility(player1, 0, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(hierophant.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The activated ability cannot target a creature")
    void activatedAbilityCannotTargetCreature() {
        Permanent hierophant = harness.addToBattlefieldAndReturn(player1, new StoneSeederHierophant());
        hierophant.setSummoningSick(false);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A land entering without being played untaps only the Hierophant")
    void landEnteringWithoutBeingPlayedUntapsOnlySelf() {
        Permanent hierophant = harness.addToBattlefieldAndReturn(player1, new StoneSeederHierophant());
        Permanent recruit = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        hierophant.tap();
        recruit.tap();

        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(hierophant.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(hierophant.isTapped()).isFalse();
        assertThat(recruit.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The activated ability can untap a land you control")
    void activatedAbilityUntapsOwnLand() {
        Permanent hierophant = harness.addToBattlefieldAndReturn(player1, new StoneSeederHierophant());
        hierophant.setSummoningSick(false);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();

        harness.activateAbility(player1, 0, 0, null, forest.getId());

        assertThat(hierophant.isTapped()).isTrue();
        assertThat(forest.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An untapped land is a legal target")
    void activatedAbilityCanTargetUntappedLand() {
        Permanent hierophant = harness.addToBattlefieldAndReturn(player1, new StoneSeederHierophant());
        hierophant.setSummoningSick(false);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, 0, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(hierophant.isTapped()).isTrue();
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Landfall does not remove summoning sickness")
    void landfallDoesNotAllowSummoningSickActivation() {
        Permanent hierophant = harness.addToBattlefieldAndReturn(player1, new StoneSeederHierophant());
        hierophant.setSummoningSick(true);
        hierophant.tap();
        Permanent forest = harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();

        assertThat(hierophant.isTapped()).isFalse();
        forest.tap();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(hierophant.isTapped()).isFalse();
        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
