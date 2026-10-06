package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IcebindPillar.class, GrizzlyBears.class, AngelsFeather.class, InSearchOfGreatness.class})
class IcebindPillarTest extends BaseCardTest {

    @Test
    @DisplayName("Taps a target creature")
    void tapsTargetCreature() {
        addReadyPillar(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addSnowMana();

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taps a target artifact")
    void tapsTargetArtifact() {
        addReadyPillar(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AngelsFeather());
        addSnowMana();

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Requires snow mana to activate")
    void requiresSnowMana() {
        addReadyPillar(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        addReadyPillar(player1);
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new InSearchOfGreatness());
        addSnowMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
    }

    @Test
    void tapsSourceAndSpendsSnowManaBeforeTargetIsTapped() {
        Permanent pillar = addReadyPillar(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IcebindPillar());
        addSnowMana();

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(pillar.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isZero();
        assertThat(target.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void newlyEnteredNoncreaturePillarCanActivateUsingColorlessSnowMana() {
        Permanent pillar = harness.addToBattlefieldAndReturn(player1, new IcebindPillar());
        pillar.setSummoningSick(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IcebindPillar());
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(pillar.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void cannotActivateAlreadyTappedPillar() {
        Permanent pillar = addReadyPillar(player1);
        pillar.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IcebindPillar());
        addSnowMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void canTargetItselfEvenThoughPayingTheCostTapsIt() {
        Permanent pillar = addReadyPillar(player1);
        addSnowMana();

        harness.activateAbility(player1, 0, null, pillar.getId());
        harness.passBothPriorities();

        assertThat(pillar.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetAnAlreadyTappedArtifactYouControl() {
        addReadyPillar(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new IcebindPillar());
        target.tap();
        addSnowMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyPillar(Player player) {
        Permanent pillar = harness.addToBattlefieldAndReturn(player, new IcebindPillar());
        pillar.setSummoningSick(false);
        return pillar;
    }

    private void addSnowMana() {
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSnowMana(ManaColor.BLUE, 1);
    }
}
