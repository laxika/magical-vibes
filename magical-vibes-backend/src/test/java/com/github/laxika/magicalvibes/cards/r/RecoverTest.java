package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.s.Shunt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCreatureFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecoverTest {

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

    @Test
    @DisplayName("Recover has correct card properties")
    void hasCorrectCardProperties() {
        Recover card = new Recover();

        assertThat(card.getName()).isEqualTo("Recover");
        assertThat(card.getType()).isEqualTo(CardType.SORCERY);
        assertThat(card.getManaCost()).isEqualTo("{2}{B}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(ReturnCreatureFromGraveyardToHandEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(DrawCardEffect.class);
    }

    @Test
    @DisplayName("Casting Recover puts a graveyard-targeted spell on the stack")
    void castingPutsGraveyardTargetedSpellOnStack() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new Recover()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetPermanentId()).isEqualTo(target.getId());
        assertThat(entry.getTargetZone()).isEqualTo(Zone.GRAVEYARD);
    }

    @Test
    @DisplayName("Resolve Recover returns targeted creature card to hand")
    void resolvesAndReturnsTargetedCreatureCard() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new Recover()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(target.getId()));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getName().equals("Recover"));
    }

    @Test
    @DisplayName("Recover cannot target non-creature card in your graveyard")
    void cannotTargetNonCreatureCardInYourGraveyard() {
        Card nonCreature = new HolyDay();
        harness.setGraveyard(player1, List.of(nonCreature));
        harness.setHand(player1, List.of(new Recover()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target card must be a creature");
    }

    @Test
    @DisplayName("Recover cannot target creature card in opponent graveyard")
    void cannotTargetCreatureCardInOpponentGraveyard() {
        Card opponentsCreature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(opponentsCreature));
        harness.setHand(player1, List.of(new Recover()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, opponentsCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }

    @Test
    @DisplayName("Recover fizzles if targeted creature card leaves graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyardBeforeResolution() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new Recover()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, target.getId());
        harness.getGameData().playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(c -> c.getId().equals(target.getId()));
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Shunt can only retarget Recover to another creature card in Recover controller graveyard")
    void shuntRetargetRespectsYourGraveyardConstraint() {
        Card currentTarget = new GrizzlyBears();
        Card legalNewTarget = new GrizzlyBears();
        Card illegalOpponentTarget = new GrizzlyBears();

        harness.setGraveyard(player1, List.of(currentTarget, legalNewTarget));
        harness.setGraveyard(player2, List.of(illegalOpponentTarget));

        Recover recover = new Recover();
        harness.setHand(player1, List.of(recover));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.setHand(player2, List.of(new Shunt()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, currentTarget.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, recover.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.awaitingPermanentChoiceValidIds()).contains(legalNewTarget.getId());
        assertThat(gd.interaction.awaitingPermanentChoiceValidIds()).doesNotContain(currentTarget.getId());
        assertThat(gd.interaction.awaitingPermanentChoiceValidIds()).doesNotContain(illegalOpponentTarget.getId());
    }
}
