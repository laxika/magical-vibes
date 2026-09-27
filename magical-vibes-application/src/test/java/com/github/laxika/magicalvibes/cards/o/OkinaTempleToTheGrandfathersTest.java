package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.k.KondaLordOfEiganjo;
import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
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

@CardUsed({OkinaTempleToTheGrandfathers.class, KondaLordOfEiganjo.class, WanderingOnes.class})
class OkinaTempleToTheGrandfathersTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Okina produces green mana")
    void tappingProducesGreenMana() {
        addReadyOkina(player1);

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability gives a legendary creature +1/+1 until end of turn")
    void boostsLegendaryCreature() {
        addReadyOkina(player1);
        Permanent konda = addCreatureReady(player1, new KondaLordOfEiganjo());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, konda.getId());
        harness.passBothPriorities();

        assertThat(konda.getEffectivePower()).isEqualTo(4);
        assertThat(konda.getEffectiveToughness()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(konda.getEffectivePower()).isEqualTo(3);
        assertThat(konda.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Ability can target an opponent's legendary creature")
    void boostsOpponentsLegendaryCreature() {
        addReadyOkina(player1);
        Permanent konda = addCreatureReady(player2, new KondaLordOfEiganjo());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, konda.getId());
        harness.passBothPriorities();

        assertThat(konda.getEffectivePower()).isEqualTo(4);
        assertThat(konda.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Ability cannot target a nonlegendary creature")
    void cannotTargetNonlegendaryCreature() {
        addReadyOkina(player1);
        Permanent wanderingOnes = addCreatureReady(player2, new WanderingOnes());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, wanderingOnes.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a legendary creature");
    }

    @Test
    @DisplayName("Ability cannot target a legendary land")
    void cannotTargetLegendaryLand() {
        addReadyOkina(player1);
        Permanent target = addReadyOkina(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a legendary creature");
    }

    @Test
    @DisplayName("Ability requires green mana and tapping Okina")
    void abilityRequiresGreenManaAndUntappedOkina() {
        Permanent okina = addReadyOkina(player1);
        Permanent konda = addCreatureReady(player1, new KondaLordOfEiganjo());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, konda.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, konda.getId());
        harness.passBothPriorities();

        assertThat(okina.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, konda.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyOkina(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new OkinaTempleToTheGrandfathers());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
