package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AdarkarWastes;
import com.github.laxika.magicalvibes.cards.k.KjeldoranSkyknight;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Blizzard.class, SnowCoveredForest.class, AdarkarWastes.class,
        KjeldoranSkyknight.class, BalduvianBears.class})
class BlizzardTest extends BaseCardTest {

    @Test
    @DisplayName("Castable while controlling a snow land")
    void castableWithSnowLand() {
        harness.addToBattlefield(player1, new SnowCoveredForest());
        harness.setHand(player1, List.of(new Blizzard()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Blizzard");
    }

    @Test
    @DisplayName("Not castable without a snow land")
    void notCastableWithoutSnowLand() {
        harness.setHand(player1, List.of(new Blizzard()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("A non-snow land does not enable casting")
    void nonSnowLandDoesNotEnableCasting() {
        harness.addToBattlefield(player1, new AdarkarWastes());
        harness.setHand(player1, List.of(new Blizzard()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Flying creatures stay tapped; non-fliers untap")
    void flyingCreaturesDontUntap() {
        harness.addToBattlefield(player1, new Blizzard());
        Permanent flier = addCreatureReady(player1, new KjeldoranSkyknight());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        flier.tap();
        bears.tap();

        harness.performUntapStep(player1);

        assertThat(flier.isTapped()).isTrue();
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent's fliers also don't untap")
    void opponentFliersDontUntap() {
        harness.addToBattlefield(player1, new Blizzard());
        Permanent flier = addCreatureReady(player2, new KjeldoranSkyknight());
        flier.tap();

        harness.performUntapStep(player2);

        assertThat(flier.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying cumulative upkeep keeps Blizzard")
    void paysCumulativeUpkeep() {
        Permanent blizzard = harness.addToBattlefieldAndReturn(player1, new Blizzard());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(blizzard.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blizzard);
    }

    @Test
    @DisplayName("Cumulative upkeep doubles on the second upkeep")
    void cumulativeUpkeepDoublesOnSecondUpkeep() {
        Permanent blizzard = harness.addToBattlefieldAndReturn(player1, new Blizzard());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(blizzard.getCounterCount(CounterType.AGE)).isEqualTo(2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blizzard);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Blizzard")
    void declineSacrifices() {
        Permanent blizzard = harness.addToBattlefieldAndReturn(player1, new Blizzard());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blizzard);
        harness.assertInGraveyard(player1, "Blizzard");
    }

    @Test
    @DisplayName("Insufficient mana sacrifices Blizzard during cumulative upkeep")
    void insufficientManaSacrifices() {
        Permanent blizzard = harness.addToBattlefieldAndReturn(player1, new Blizzard());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blizzard);
        harness.assertInGraveyard(player1, "Blizzard");
    }
}
