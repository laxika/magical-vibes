package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DuergarHedgeMage;
import com.github.laxika.magicalvibes.cards.f.FangSkulkin;
import com.github.laxika.magicalvibes.cards.f.FlameJab;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CinderPyromancer.class, FlameJab.class, FangSkulkin.class, ChandraNalaar.class, DuergarHedgeMage.class})
class CinderPyromancerTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability deals 1 damage to target player")
    void tapAbilityDealsDamageToPlayer() {
        addReadyPyromancer(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Tap ability deals 1 damage to a planeswalker")
    void tapAbilityDealsDamageToPlaneswalker() {
        addReadyPyromancer(player1);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 2);

        harness.activateAbility(player1, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tap ability requires tap — cannot activate when already tapped")
    void tapAbilityRequiresTap() {
        Permanent pyromancer = addReadyPyromancer(player1);
        pyromancer.tap();

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> harness.activateAbility(player1, 0, null, player2.getId())
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting a red spell lets you untap this creature")
    void redSpellUntapsPyromancer() {
        Permanent pyromancer = addReadyPyromancer(player1);
        pyromancer.tap();
        harness.setHand(player1, List.of(new FlameJab()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // resolve untap trigger

        assertThat(pyromancer.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the trigger leaves this creature tapped")
    void decliningLeavesPyromancerTapped() {
        Permanent pyromancer = addReadyPyromancer(player1);
        pyromancer.tap();
        harness.setHand(player1, List.of(new FlameJab()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(pyromancer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting a non-red spell does not trigger the untap")
    void nonRedSpellDoesNotTrigger() {
        Permanent pyromancer = addReadyPyromancer(player1);
        pyromancer.tap();
        harness.setHand(player1, List.of(new FangSkulkin()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(pyromancer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A red spell cast by an opponent does not trigger the untap")
    void opponentsRedSpellDoesNotTrigger() {
        Permanent pyromancer = addReadyPyromancer(player1);
        pyromancer.tap();
        harness.setHand(player2, List.of(new FlameJab()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.passBothPriorities();

        assertThat(pyromancer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Hybrid red spell paid entirely with white mana triggers the untap")
    void hybridRedSpellPaidWithWhiteTriggersUntap() {
        Permanent pyromancer = addReadyPyromancer(player1);
        pyromancer.tap();
        harness.setHand(player1, List.of(new DuergarHedgeMage()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(pyromancer.isTapped()).isFalse();
        harness.assertNotOnBattlefield(player1, "Duergar Hedge-Mage");
    }

    @Test
    @DisplayName("The red spell trigger untaps only its source")
    void untapDoesNotUntapOtherPermanents() {
        Permanent pyromancer = addReadyPyromancer(player1);
        Permanent other = addCreatureReady(player1, new FangSkulkin());
        pyromancer.tap();
        other.tap();
        harness.setHand(player1, List.of(new FlameJab()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(pyromancer.isTapped()).isFalse();
        assertThat(other.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A summoning-sick Pyromancer cannot activate its tap ability")
    void summoningSicknessPreventsTapAbility() {
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player1, new CinderPyromancer());
        pyromancer.setSummoningSick(true);

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> harness.activateAbility(player1, 0, null, player2.getId())
        ).isInstanceOf(IllegalStateException.class);

        assertThat(pyromancer.isTapped()).isFalse();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Tap ability can target its controller")
    void tapAbilityCanDamageController() {
        addReadyPyromancer(player1);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Tap ability cannot target a creature")
    void tapAbilityRejectsCreatureTarget() {
        Permanent pyromancer = addReadyPyromancer(player1);
        Permanent creature = addCreatureReady(player2, new FangSkulkin());

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> harness.activateAbility(player1, 0, null, creature.getId())
        ).isInstanceOf(IllegalStateException.class);

        assertThat(pyromancer.isTapped()).isFalse();
        harness.assertOnBattlefield(player2, "Fang Skulkin");
    }

    private Permanent addReadyPyromancer(Player player) {
        return addCreatureReady(player, new CinderPyromancer());
    }
}
