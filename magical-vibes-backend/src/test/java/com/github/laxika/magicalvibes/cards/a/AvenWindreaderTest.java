package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AvenWindreaderTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Aven Windreader has correct card properties")
    void hasCorrectProperties() {
        AvenWindreader card = new AvenWindreader();

        assertThat(card.getName()).isEqualTo("Aven Windreader");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{3}{U}{U}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLUE);
        assertThat(card.getPower()).isEqualTo(3);
        assertThat(card.getToughness()).isEqualTo(3);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.BIRD, CardSubtype.SOLDIER, CardSubtype.WIZARD);
        assertThat(card.getKeywords()).containsExactly(Keyword.FLYING);
        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{1}{U}");
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().getFirst().isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().getFirst().getEffects().getFirst()).isInstanceOf(RevealTopCardOfLibraryEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Aven Windreader puts it on the stack and resolves to battlefield")
    void castAndResolve() {
        harness.setHand(player1, List.of(new AvenWindreader()));
        harness.addMana(player1, "U", 2);
        harness.addMana(player1, "W", 3);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Aven Windreader");

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Aven Windreader"));
    }

    // ===== Activated ability: reveal top card =====

    @Test
    @DisplayName("Activating ability targeting opponent reveals top card of their library in game log")
    void revealOpponentTopCard() {
        harness.addToBattlefield(player1, new AvenWindreader());
        harness.addMana(player1, "U", 1);
        harness.addMana(player1, "W", 1);

        // Note the top card of player2's deck
        GameData gd = harness.getGameData();
        String topCardName = gd.playerDecks.get(player2.getId()).getFirst().getName();
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("reveals") && log.contains(topCardName));
        // Card stays on top — deck size unchanged
        assertThat(gd.playerDecks.get(player2.getId()).size()).isEqualTo(deckSizeBefore);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo(topCardName);
    }

    @Test
    @DisplayName("Activating ability targeting self reveals top card of own library in game log")
    void revealOwnTopCard() {
        harness.addToBattlefield(player1, new AvenWindreader());
        harness.addMana(player1, "U", 1);
        harness.addMana(player1, "W", 1);

        GameData gd = harness.getGameData();
        String topCardName = gd.playerDecks.get(player1.getId()).getFirst().getName();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("reveals") && log.contains(topCardName));
        assertThat(gd.playerDecks.get(player1.getId()).size()).isEqualTo(deckSizeBefore);
    }

    @Test
    @DisplayName("Revealing top card when target player's library is empty logs appropriate message")
    void revealEmptyLibrary() {
        harness.addToBattlefield(player1, new AvenWindreader());
        harness.addMana(player1, "U", 1);
        harness.addMana(player1, "W", 1);

        // Empty player2's deck
        GameData gd = harness.getGameData();
        gd.playerDecks.put(player2.getId(), new ArrayList<>());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("library is empty"));
    }

    @Test
    @DisplayName("Ability can be activated multiple times per turn (no tap required)")
    void activateMultipleTimes() {
        harness.addToBattlefield(player1, new AvenWindreader());
        harness.addMana(player1, "U", 2);
        harness.addMana(player1, "W", 2);

        // First activation
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        long revealCount = gd.gameLog.stream().filter(log -> log.contains("reveals")).count();
        assertThat(revealCount).isEqualTo(1);

        // Second activation — same permanent, not tapped
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        long revealCountAfter = gd.gameLog.stream().filter(log -> log.contains("reveals")).count();
        assertThat(revealCountAfter).isEqualTo(2);
    }

    // ===== Validation =====

    @Test
    @DisplayName("Activating ability without a target throws exception")
    void activateWithoutTarget() {
        harness.addToBattlefield(player1, new AvenWindreader());
        harness.addMana(player1, "U", 1);
        harness.addMana(player1, "W", 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("target player");
    }

    @Test
    @DisplayName("Activating ability targeting a permanent instead of a player throws exception")
    void activateTargetingPermanent() {
        harness.addToBattlefield(player1, new AvenWindreader());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, "U", 1);
        harness.addMana(player1, "W", 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be a player");
    }
}
