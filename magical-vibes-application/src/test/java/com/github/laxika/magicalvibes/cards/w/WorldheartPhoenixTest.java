package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorldheartPhoenixTest extends BaseCardTest {

    private Permanent phoenixOnBattlefield() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Worldheart Phoenix"))
                .findFirst().orElseThrow();
    }

    private void addWubrg() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    // ===== Cast from hand =====

    @Test
    @DisplayName("Cast from hand enters with no counters")
    void castFromHandNoCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new WorldheartPhoenix()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Worldheart Phoenix");
        assertThat(phoenixOnBattlefield().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    // ===== Cast from graveyard =====

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
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Worldheart Phoenix"));
    }
}
