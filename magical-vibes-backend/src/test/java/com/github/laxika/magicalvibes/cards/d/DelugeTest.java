package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.TapCreaturesEffect;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DelugeTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Deluge has correct card properties")
    void hasCorrectProperties() {
        Deluge card = new Deluge();

        assertThat(card.getName()).isEqualTo("Deluge");
        assertThat(card.getType()).isEqualTo(CardType.INSTANT);
        assertThat(card.getManaCost()).isEqualTo("{2}{U}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLUE);
        assertThat(card.isNeedsTarget()).isFalse();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(TapCreaturesEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack as INSTANT_SPELL")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new Deluge()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Deluge");
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving taps all creatures without flying on both sides")
    void tapsAllCreaturesWithoutFlying() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Deluge()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<Permanent> p1Battlefield = gd.playerBattlefields.get(player1.getId());
        for (Permanent p : p1Battlefield) {
            if (p.getCard().getType() == CardType.CREATURE) {
                assertThat(p.isTapped()).isTrue();
            }
        }

        List<Permanent> p2Battlefield = gd.playerBattlefields.get(player2.getId());
        for (Permanent p : p2Battlefield) {
            if (p.getCard().getType() == CardType.CREATURE) {
                assertThat(p.isTapped()).isTrue();
            }
        }
    }

    @Test
    @DisplayName("Does not tap creatures with flying")
    void doesNotTapCreaturesWithFlying() {
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Deluge()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        Permanent airElemental = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Air Elemental"))
                .findFirst().orElseThrow();
        Permanent grizzlyBears = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(airElemental.isTapped()).isFalse();
        assertThat(grizzlyBears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Works with empty battlefield (no crash)")
    void worksWithEmptyBattlefield() {
        harness.setHand(player1, List.of(new Deluge()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Deluge goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new Deluge()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Deluge"));
    }

    @Test
    @DisplayName("Does not tap creatures with flying on opponent's side either")
    void doesNotTapOpponentFlyingCreatures() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Deluge()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player2.getId());
        Permanent airElemental = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Air Elemental"))
                .findFirst().orElseThrow();
        Permanent grizzlyBears = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(airElemental.isTapped()).isFalse();
        assertThat(grizzlyBears.isTapped()).isTrue();
    }
}
