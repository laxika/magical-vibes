package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.h.HavengulLich;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WorldheartPhoenix.class, HavengulLich.class})
class WorldheartPhoenixTest extends BaseCardTest {

    private Permanent phoenixOnBattlefield() {
        return findPermanent(player1, "Worldheart Phoenix");
    }

    private void addWubrg() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    @Test
    @DisplayName("Cast from hand enters with no counters")
    void castFromHandNoCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new WorldheartPhoenix(), "{3}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Worldheart Phoenix");
        assertThat(phoenixOnBattlefield().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cast from graveyard for {W}{U}{B}{R}{G} enters with two +1/+1 counters")
    void castFromGraveyardEntersWithCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new WorldheartPhoenix()));
        addWubrg();

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Worldheart Phoenix");
        Permanent phoenix = phoenixOnBattlefield();
        assertThat(phoenix.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        // 2/2 base + two +1/+1 counters = 4/4
        assertThat(phoenix.getEffectivePower()).isEqualTo(4);
        assertThat(phoenix.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cast from graveyard does not exile the card (goes to battlefield, not exile)")
    void castFromGraveyardDoesNotExile() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new WorldheartPhoenix()));
        addWubrg();

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Worldheart Phoenix");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Worldheart Phoenix"));
    }

    @Test
    @DisplayName("Cannot cast from graveyard without paying the {W}{U}{B}{R}{G} alternate cost")
    void cannotCastFromGraveyardWithoutColoredMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new WorldheartPhoenix()));
        // Enough to pay the normal {3}{R} cost, but not the graveyard alternate {W}{U}{B}{R}{G}.
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        // Card stays in the graveyard; nothing entered the battlefield.
        harness.assertInGraveyard(player1, "Worldheart Phoenix");
    }

    @Test
    @DisplayName("Casting with Havengul Lich permission at normal cost grants no counters")
    void otherGraveyardPermissionDoesNotGrantCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new HavengulLich());
        WorldheartPhoenix phoenix = new WorldheartPhoenix();
        harness.setGraveyard(player1, List.of(phoenix));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, phoenix.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromGraveyard(player1, phoenix.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Worldheart Phoenix");
        assertThat(phoenixOnBattlefield().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Graveyard permission does not allow casting during combat")
    void cannotCastFromGraveyardOutsideMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.setGraveyard(player1, List.of(new WorldheartPhoenix()));
        addWubrg();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Worldheart Phoenix");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Graveyard permission does not allow casting with a spell on the stack")
    void cannotCastFromGraveyardWithNonemptyStack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new WorldheartPhoenix(), "{3}{R}");
        harness.setGraveyard(player1, List.of(new WorldheartPhoenix()));
        addWubrg();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Worldheart Phoenix");
        assertThat(gd.stack).hasSize(1);
    }
}
