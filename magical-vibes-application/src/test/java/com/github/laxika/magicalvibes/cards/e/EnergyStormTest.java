package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.f.Flare;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.k.KjeldoranSkycaptain;
import com.github.laxika.magicalvibes.cards.l.LavaBurst;
import com.github.laxika.magicalvibes.cards.s.StormSpirit;
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

@CardUsed({EnergyStorm.class, BalduvianBears.class, Flare.class, Hurricane.class, Incinerate.class,
        KjeldoranSkycaptain.class, LavaBurst.class, StormSpirit.class})
class EnergyStormTest extends BaseCardTest {

    private void advanceToNextTurn(Player currentActivePlayer) {
        Player nextActivePlayer = currentActivePlayer.equals(player1) ? player2 : player1;
        harness.forceActivePlayer(currentActivePlayer);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(nextActivePlayer, TurnStep.UNTAP);
    }

    @Test
    @DisplayName("Prevents Flare damage to a player")
    void preventsInstantDamageToPlayer() {
        harness.addToBattlefield(player1, new EnergyStorm());
        harness.setHand(player2, List.of(new Flare()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Prevents Hurricane damage to both players")
    void preventsSorceryDamageToPlayers() {
        harness.addToBattlefield(player1, new EnergyStorm());
        harness.setHand(player2, List.of(new Hurricane()));
        harness.addMana(player2, ManaColor.GREEN, 3);

        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, 2);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Prevents Incinerate damage to a creature")
    void preventsInstantDamageToCreature() {
        harness.addToBattlefield(player1, new EnergyStorm());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        assertThat(bears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not prevent combat damage")
    void doesNotPreventCombatDamage() {
        harness.addToBattlefield(player1, new EnergyStorm());
        addCreatureReady(player2, new BalduvianBears());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Flying creatures stay tapped; non-fliers untap")
    void flyingCreaturesDontUntap() {
        harness.addToBattlefield(player1, new EnergyStorm());
        Permanent flier = addCreatureReady(player1, new KjeldoranSkycaptain());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        flier.tap();
        bears.tap();

        advanceToNextTurn(player2);

        assertThat(flier.isTapped()).isTrue();
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Flying creatures controlled by an opponent also stay tapped")
    void opponentFlyingCreaturesDontUntap() {
        harness.addToBattlefield(player1, new EnergyStorm());
        Permanent flier = addCreatureReady(player2, new KjeldoranSkycaptain());
        flier.tap();

        advanceToNextTurn(player1);

        assertThat(flier.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not prevent damage from an activated ability")
    void doesNotPreventActivatedAbilityDamage() {
        harness.addToBattlefield(player1, new EnergyStorm());
        Permanent spirit = addCreatureReady(player1, new StormSpirit());
        Permanent bears = addCreatureReady(player2, new BalduvianBears());

        int spiritIndex = gd.playerBattlefields.get(player1.getId()).indexOf(spirit);
        harness.activateAbility(player1, spiritIndex, null, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Balduvian Bears");
    }

    @Test
    @DisplayName("Does not prevent spell damage that cannot be prevented")
    void doesNotPreventUnpreventableSpellDamage() {
        harness.addToBattlefield(player1, new EnergyStorm());
        Permanent bears = addCreatureReady(player2, new BalduvianBears());
        harness.setHand(player1, List.of(new LavaBurst()));
        harness.addMana(player1, ManaColor.RED, 2); // X=1 + {R}

        harness.castSorcery(player1, 0, 1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Paying cumulative upkeep keeps Energy Storm")
    void paysCumulativeUpkeep() {
        Permanent storm = harness.addToBattlefieldAndReturn(player1, new EnergyStorm());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(storm.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(storm);
    }

    @Test
    @DisplayName("Cumulative upkeep increases with each age counter")
    void cumulativeUpkeepIncreasesEachUpkeep() {
        Permanent storm = harness.addToBattlefieldAndReturn(player1, new EnergyStorm());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        advanceToNextTurn(player2);
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(storm.getCounterCount(CounterType.AGE)).isEqualTo(2);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(storm);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Energy Storm")
    void declineSacrifices() {
        Permanent storm = harness.addToBattlefieldAndReturn(player1, new EnergyStorm());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(storm);
        harness.assertInGraveyard(player1, "Energy Storm");
    }
}
