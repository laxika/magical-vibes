package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JayemdaeTomeTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Jayemdae Tome has correct card properties")
    void hasCorrectProperties() {
        JayemdaeTome card = new JayemdaeTome();

        assertThat(card.getName()).isEqualTo("Jayemdae Tome");
        assertThat(card.getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(card.getManaCost()).isEqualTo("{4}");
        assertThat(card.getColor()).isNull();
        assertThat(card.getPower()).isNull();
        assertThat(card.getToughness()).isNull();
        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isEqualTo("{4}");
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(DrawCardEffect.class);
        DrawCardEffect effect = (DrawCardEffect) ability.getEffects().getFirst();
        assertThat(effect.amount()).isEqualTo(1);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new JayemdaeTome()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Jayemdae Tome");
    }

    @Test
    @DisplayName("Resolving puts it on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new JayemdaeTome()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Jayemdae Tome"));
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new JayemdaeTome()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Activated ability =====

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingPutsOnStack() {
        addReadyTome(player1);
        harness.addMana(player1, ManaColor.WHITE, 4);
        setDeck(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Jayemdae Tome");
    }

    @Test
    @DisplayName("Activating ability taps Jayemdae Tome")
    void activatingTapsTome() {
        Permanent tome = addReadyTome(player1);
        harness.addMana(player1, ManaColor.WHITE, 4);
        setDeck(player1, List.of(new Forest()));

        assertThat(tome.isTapped()).isFalse();

        harness.activateAbility(player1, 0, null, null);

        assertThat(tome.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addReadyTome(player1);
        harness.addMana(player1, ManaColor.WHITE, 6);
        setDeck(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Resolving ability draws a card")
    void resolvingDrawsACard() {
        addReadyTome(player1);
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        setDeck(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId()).get(1).getName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("Resolving ability does not affect opponent's hand")
    void doesNotAffectOpponent() {
        addReadyTome(player1);
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        setDeck(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Resolving ability logs the card draw")
    void resolvingLogsCardDraw() {
        addReadyTome(player1);
        harness.addMana(player1, ManaColor.WHITE, 4);
        setDeck(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("draws a card"));
    }

    @Test
    @DisplayName("Drawing from empty deck is handled")
    void drawingFromEmptyDeck() {
        addReadyTome(player1);
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.setHand(player1, List.of());
        gd.playerDecks.get(player1.getId()).clear();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no cards to draw"));
    }

    // ===== Validation =====

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyTome(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate twice in a turn because it requires tap")
    void cannotActivateTwice() {
        addReadyTome(player1);
        harness.addMana(player1, ManaColor.WHITE, 8);
        setDeck(player1, List.of(new Forest(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent tome = addReadyTome(player1);
        tome.tap();
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== No summoning sickness for artifacts =====

    @Test
    @DisplayName("Can activate ability the turn it enters the battlefield (no summoning sickness for artifacts)")
    void noSummoningSicknessForArtifact() {
        JayemdaeTome card = new JayemdaeTome();
        Permanent tome = new Permanent(card);
        tome.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(tome);
        harness.addMana(player1, ManaColor.WHITE, 4);
        setDeck(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(tome.isTapped()).isTrue();
    }

    // ===== Tome stays on battlefield =====

    @Test
    @DisplayName("Jayemdae Tome remains on battlefield after activation and resolution")
    void remainsOnBattlefieldAfterResolution() {
        addReadyTome(player1);
        harness.addMana(player1, ManaColor.WHITE, 4);
        setDeck(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Jayemdae Tome"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Jayemdae Tome"));
    }

    // ===== Helpers =====

    private Permanent addReadyTome(Player player) {
        JayemdaeTome card = new JayemdaeTome();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setDeck(Player player, List<? extends com.github.laxika.magicalvibes.model.Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}

