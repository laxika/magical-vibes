package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DevotedRetainer;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({ShinkaTheBloodsoakedKeep.class, IsamaruHoundOfKonda.class, DevotedRetainer.class})
class ShinkaTheBloodsoakedKeepTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Shinka produces red mana")
    void tappingProducesRedMana() {
        Permanent shinka = addReadyShinka(player1);

        harness.tapPermanent(player1, 0);

        assertThat(shinka.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability gives a legendary creature first strike until end of turn")
    void grantsFirstStrikeToLegendaryCreature() {
        Permanent shinka = addReadyShinka(player1);
        Permanent legendaryCreature = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThat(gqs.hasKeyword(gd, legendaryCreature, Keyword.FIRST_STRIKE)).isFalse();

        harness.activateAbility(player1, 0, null, legendaryCreature.getId());
        harness.passBothPriorities();

        assertThat(shinka.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gqs.hasKeyword(gd, legendaryCreature, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, legendaryCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Ability can target an opponent's legendary creature")
    void grantsFirstStrikeToOpponentsLegendaryCreature() {
        addReadyShinka(player1);
        Permanent legendaryCreature = harness.addToBattlefieldAndReturn(player2, new IsamaruHoundOfKonda());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, legendaryCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, legendaryCreature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Ability cannot target a nonlegendary creature")
    void cannotTargetNonlegendaryCreature() {
        Permanent shinka = addReadyShinka(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DevotedRetainer());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a legendary creature");

        assertThat(shinka.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability cannot target a legendary noncreature permanent")
    void cannotTargetLegendaryNoncreaturePermanent() {
        Permanent shinka = addReadyShinka(player1);
        Permanent legendaryLand = harness.addToBattlefieldAndReturn(player2, new ShinkaTheBloodsoakedKeep());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, legendaryLand.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a legendary creature");

        assertThat(shinka.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    private Permanent addReadyShinka(Player player) {
        Permanent permanent = new Permanent(new ShinkaTheBloodsoakedKeep());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
