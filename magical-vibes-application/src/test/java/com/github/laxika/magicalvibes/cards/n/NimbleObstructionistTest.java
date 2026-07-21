package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FumeSpitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NimbleObstructionistTest extends BaseCardTest {

    private void addCyclingMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.addMana(player, ManaColor.BLUE, 1);
    }

    // ===== Cycling trigger: counter an opponent's activated ability, then draw =====

    @Test
    @DisplayName("Cycling counters an opponent's activated ability and still draws")
    void cyclingCountersOpponentActivatedAbilityAndDraws() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        FumeSpitter fumeSpitter = new FumeSpitter();
        harness.addToBattlefield(player2, fumeSpitter);

        harness.setHand(player1, List.of(new NimbleObstructionist()));
        harness.setLibrary(player1, List.of(new SerraAngel()));
        addCyclingMana(player1);

        // Player2 activates Fume Spitter's ability targeting player1's Grizzly Bears.
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player2);

        // Player1 cycles Nimble Obstructionist, countering Fume Spitter's ability.
        harness.activateHandAbility(player1, 0, fumeSpitter.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Ability countered: Grizzly Bears never received the -1/-1 counter and survives.
        assertThat(findPermanent(player1, "Grizzly Bears").getCounterCount(CounterType.MINUS_ONE_MINUS_ONE))
                .isZero();

        // Cycling still draws: Nimble Obstructionist is discarded, the top of library is drawn.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Nimble Obstructionist"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Serra Angel"));

        assertThat(gd.stack).isEmpty();
    }

    // ===== Cycling with no ability to counter still draws =====

    @Test
    @DisplayName("Cycling with no legal target still draws a card")
    void cyclingWithoutTargetStillDraws() {
        harness.setHand(player1, List.of(new NimbleObstructionist()));
        harness.setLibrary(player1, List.of(new SerraAngel()));
        addCyclingMana(player1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Nimble Obstructionist"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Serra Angel"));
    }

    // ===== Cannot counter an ability you control =====

    @Test
    @DisplayName("Cannot target an activated ability you control")
    void cannotCounterOwnAbility() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player2, bears);

        FumeSpitter fumeSpitter = new FumeSpitter();
        harness.addToBattlefield(player1, fumeSpitter);

        harness.setHand(player1, List.of(new NimbleObstructionist()));
        harness.setLibrary(player1, List.of(new SerraAngel()));
        addCyclingMana(player1);

        // Player1 activates their own Fume Spitter targeting player2's Grizzly Bears.
        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears"));

        // Player1 cannot cycle to counter their own ability.
        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, fumeSpitter.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Cannot counter a spell (only activated/triggered abilities) =====

    @Test
    @DisplayName("Cannot target a spell on the stack")
    void cannotCounterSpell() {
        harness.setHand(player1, List.of(new NimbleObstructionist()));
        harness.setLibrary(player1, List.of(new SerraAngel()));
        addCyclingMana(player1);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        // Player2 casts Shock targeting player1.
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        // Nimble Obstructionist can only counter abilities, not spells.
        UUID shockId = shock.getId();
        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, shockId))
                .isInstanceOf(IllegalStateException.class);
    }
}
