package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HinderingLightTest extends BaseCardTest {

    // ===== Counter a spell targeting you (the player) =====

    @Test
    @DisplayName("Counters a spell targeting you and draws a card")
    void countersSpellTargetingYouAndDraws() {
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.setHand(player1, List.of(new HinderingLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        // Player2 casts Shock targeting player1 (the Hindering Light caster)
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        // Player1 casts Hindering Light targeting Shock
        harness.castInstant(player1, 0, shock.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Shock countered
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
        // Player1 life untouched
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        // Player1 drew a card (hand emptied by casting, then drew 1)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    // ===== Counter a spell targeting a permanent you control =====

    @Test
    @DisplayName("Counters a spell targeting a permanent you control")
    void countersSpellTargetingYourPermanent() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.setHand(player1, List.of(new HinderingLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player2);

        harness.castInstant(player1, 0, shock.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Illegal targets =====

    @Test
    @DisplayName("Cannot target a spell targeting the opponent player")
    void cannotTargetSpellTargetingOpponent() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock, new HinderingLight()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        // Player1 casts Shock targeting player2 (the opponent)
        harness.castInstant(player1, 0, player2.getId());

        // Hindering Light cannot target a spell aimed at the opponent
        assertThatThrownBy(() -> harness.castInstant(player1, 0, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a spell targeting an opponent's permanent")
    void cannotTargetSpellTargetingOpponentsPermanent() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player2, bears);

        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock, new HinderingLight()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
