package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlizzardTest extends BaseCardTest {

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void addSnowLand(Player player) {
        Permanent snowLand = new Permanent(new Forest());
        TestCards.mutableCard(snowLand).setSupertypes(EnumSet.of(CardSupertype.BASIC, CardSupertype.SNOW));
        gd.playerBattlefields.get(player.getId()).add(snowLand);
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn
    }

    @Test
    @DisplayName("Castable while controlling a snow land")
    void castableWithSnowLand() {
        addSnowLand(player1);
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
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new Blizzard()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Flying creatures stay tapped; non-fliers untap")
    void flyingCreaturesDontUntap() {
        addReady(player1, new Blizzard());
        Permanent flier = addReady(player1, new AirElemental());
        Permanent bears = addReady(player1, new GrizzlyBears());
        flier.tap();
        bears.tap();

        advanceToNextTurn(player2);

        assertThat(flier.isTapped()).isTrue();
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent's fliers also don't untap")
    void opponentFliersDontUntap() {
        addReady(player1, new Blizzard());
        Permanent flier = addReady(player2, new AirElemental());
        flier.tap();

        advanceToNextTurn(player1);

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

        advanceToNextTurn(player2);
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
}
