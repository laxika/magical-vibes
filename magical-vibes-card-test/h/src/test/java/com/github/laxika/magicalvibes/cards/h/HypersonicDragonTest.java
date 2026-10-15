package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HypersonicDragon.class, Divination.class, GrizzlyBears.class})
class HypersonicDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast sorcery during opponent's turn with Hypersonic Dragon on battlefield")
    void canCastSorceryDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new HypersonicDragon());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.passPriority(player2);

        harness.castSorcery(player1, 0, List.of());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Divination");
    }

    @Test
    @DisplayName("Can cast sorcery during combat with Hypersonic Dragon on battlefield")
    void canCastSorceryDuringCombat() {
        harness.addToBattlefield(player1, new HypersonicDragon());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, List.of());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Divination");
    }

    @Test
    @DisplayName("Cannot cast non-sorcery creature at instant speed with Hypersonic Dragon")
    void cannotCastNonSorceryAtInstantSpeed() {
        harness.addToBattlefield(player1, new HypersonicDragon());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Sorcery spells cannot be cast at instant speed without Hypersonic Dragon")
    void cannotCastSorceryAtInstantSpeedWithoutDragon() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Sorcery spells lose flash timing when Hypersonic Dragon leaves the battlefield")
    void sorceryLosesFlashWhenDragonLeaves() {
        harness.addToBattlefield(player1, new HypersonicDragon());

        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Hypersonic Dragon only grants flash to its controller's sorcery spells")
    void onlyAffectsController() {
        harness.addToBattlefield(player2, new HypersonicDragon());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Multiple sorceries can be cast during combat with a nonempty stack")
    void canCastMultipleSorceriesWithNonemptyStack() {
        harness.addToBattlefield(player1, new HypersonicDragon());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Divination(), new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0);
        harness.castSorcery(player1, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A Dragon in hand does not grant instant timing to sorceries")
    void dragonInHandDoesNotGrantFlash() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Divination(), new HypersonicDragon()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("A sorcery already cast resolves normally after the Dragon leaves")
    void sorceryResolvesAfterDragonLeaves() {
        harness.addToBattlefield(player1, new HypersonicDragon());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Divination()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0);

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Divination");
    }

    @Test
    @DisplayName("Haste allows a newly cast Dragon to attack")
    void newlyCastDragonCanAttack() {
        harness.setHand(player1, List.of(new HypersonicDragon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isAttacking()).isTrue();
    }

    @Test
    @DisplayName("A creature without flying or reach cannot block the Dragon")
    void groundCreatureCannotBlockDragon() {
        harness.addToBattlefield(player1, new HypersonicDragon());
        harness.addToBattlefield(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }
}
