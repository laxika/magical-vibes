package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VorinclexMonstrousRaider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulcoilViper.class, GrizzlyBears.class, Forest.class, VorinclexMonstrousRaider.class})
class SoulcoilViperTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and returns a target creature with a finality counter")
    void sacrificesSelfAndReturnsCreatureWithFinalityCounter() {
        Permanent viper = addViperReady(player1);
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(viper);
    }

    @Test
    @DisplayName("Rejects a noncreature graveyard target without paying costs")
    void rejectsNoncreatureTarget() {
        Permanent viper = addViperReady(player1);
        Card target = new Forest();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(viper);
        assertThat(viper.isTapped()).isFalse();
    }

    @Test
    void paysSacrificeBeforeResolution() {
        Permanent viper = addViperReady(player1);
        Card target = new SoulcoilViper();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(viper);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(viper.getCard(), target);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Soulcoil Viper").getCard().getId()).isEqualTo(target.getId());
    }

    @Test
    void rejectsOpponentsGraveyard() {
        Permanent viper = addViperReady(player1);
        Card target = new SoulcoilViper();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(viper);
        assertThat(viper.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetItselfWhilePayingSacrificeCost() {
        Permanent viper = addViperReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, viper.getCard().getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(viper);
    }

    @Test
    void rejectsActivationDuringCombat() {
        Permanent viper = addViperReady(player1);
        Card target = new SoulcoilViper();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        enterMainWithPriority(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(viper);
    }

    @Test
    void rejectsActivationOnOpponentsTurn() {
        Permanent viper = addViperReady(player1);
        Card target = new SoulcoilViper();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        enterMainWithPriority(player2);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(viper);
    }

    @Test
    void rejectsSummoningSickSource() {
        Permanent viper = harness.addToBattlefieldAndReturn(player1, new SoulcoilViper());
        Card target = new SoulcoilViper();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(viper);
    }

    @Test
    void rejectsTappedSource() {
        Permanent viper = addViperReady(player1);
        viper.tap();
        Card target = new SoulcoilViper();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(viper);
    }

    @Test
    void rejectsActivationWithoutBlackMana() {
        Permanent viper = addViperReady(player1);
        Card target = new SoulcoilViper();
        harness.setGraveyard(player1, List.of(target));
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(viper);
        assertThat(viper.isTapped()).isFalse();
    }

    @Test
    void fizzlesWhenTargetLeavesGraveyard() {
        Permanent viper = addViperReady(player1);
        Card target = new SoulcoilViper();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        enterMainWithPriority(player1);
        harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD);
        harness.setGraveyard(player1, List.of(viper.getCard()));
        harness.setExile(player1, List.of(target));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Soulcoil Viper");
        assertThat(gd.findExiledCard(target.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(viper.getCard());
    }

    @Test
    void finalityExilesReturnedCreatureWhenItWouldDie() {
        addViperReady(player1);
        Card target = new SoulcoilViper();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        enterMainWithPriority(player1);
        harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        Permanent returned = findPermanent(player1, "Soulcoil Viper");
        returned.setMarkedDamage(3);

        harness.runStateBasedActions();

        assertThat(gd.findExiledCard(target.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(target);
        harness.assertNotOnBattlefield(player1, "Soulcoil Viper");
    }

    @Test
    void opponentVorinclexPreventsFinalityCounter() {
        addViperReady(player1);
        harness.addToBattlefield(player2, new VorinclexMonstrousRaider());
        Card target = new SoulcoilViper();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Soulcoil Viper").getCounterCount(CounterType.FINALITY)).isZero();
    }

    @Test
    void rejectsActivationWithNonemptyStack() {
        addViperReady(player1);
        Permanent secondViper = addViperReady(player1);
        Card target = new SoulcoilViper();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 2);
        enterMainWithPriority(player1);
        harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(secondViper);
        assertThat(secondViper.isTapped()).isFalse();
        harness.passBothPriorities();
    }

    private void enterMainWithPriority(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Permanent addViperReady(Player player) {
        return addCreatureReady(player, new SoulcoilViper());
    }
}
