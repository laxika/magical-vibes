package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OrcishVeteran;
import com.github.laxika.magicalvibes.cards.z.ZhurTaaGoblin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinTown.class, ZhurTaaGoblin.class, OrcishVeteran.class, GrizzlyBears.class})
class GoblinTownTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped")
    void entersTapped() {
        Permanent town = harness.enterBattlefieldAndReturn(player1, new GoblinTown());

        assertThat(town.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana ability adds black or red mana")
    void manaAbilityAddsChosenMana() {
        Permanent town = addReadyTown();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(town.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrifice ability puts two counters on a Goblin")
    void sacrificeAbilityBoostsGoblin() {
        Permanent town = addReadyTown();
        Permanent goblin = addReadyPermanent(player1, new ZhurTaaGoblin());
        addManaForSacrificeAbility();
        readyMainPhase();

        harness.activateAbility(player1, battlefieldIndex(town), 1, null, goblin.getId());
        harness.passBothPriorities();

        assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(town);
        harness.assertInGraveyard(player1, "Goblin-town");
    }

    @Test
    @DisplayName("Sacrifice ability also targets an Orc")
    void sacrificeAbilityBoostsOrc() {
        Permanent town = addReadyTown();
        Permanent orc = addReadyPermanent(player1, new OrcishVeteran());
        addManaForSacrificeAbility();
        readyMainPhase();

        harness.activateAbility(player1, battlefieldIndex(town), 1, null, orc.getId());
        harness.passBothPriorities();

        assertThat(orc.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrifice ability cannot target a non-Goblin or non-Orc")
    void sacrificeAbilityRejectsOtherCreature() {
        Permanent town = addReadyTown();
        Permanent bear = addReadyPermanent(player1, new GrizzlyBears());
        addManaForSacrificeAbility();
        readyMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(town), 1, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifice ability can only be activated as a sorcery")
    void sacrificeAbilityIsSorcerySpeedOnly() {
        Permanent town = addReadyTown();
        Permanent goblin = addReadyPermanent(player1, new ZhurTaaGoblin());
        addManaForSacrificeAbility();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(town), 1, null, goblin.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyTown() {
        Permanent town = new Permanent(new GoblinTown());
        town.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(town);
        return town;
    }

    private Permanent addReadyPermanent(com.github.laxika.magicalvibes.model.Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addManaForSacrificeAbility() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void readyMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
