package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ShelteringLightTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has GrantKeywordEffect(INDESTRUCTIBLE, TARGET) and ScryEffect(1) on SPELL slot")
    void hasCorrectProperties() {
        ShelteringLight card = new ShelteringLight();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);

        GrantKeywordEffect grant = (GrantKeywordEffect) card.getEffects(EffectSlot.SPELL).get(0);
        assertThat(grant.keywords()).containsExactly(Keyword.INDESTRUCTIBLE);
        assertThat(grant.scope()).isEqualTo(GrantScope.TARGET);

        ScryEffect scry = (ScryEffect) card.getEffects(EffectSlot.SPELL).get(1);
        assertThat(scry.count()).isEqualTo(1);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Sheltering Light puts it on the stack targeting a creature")
    void castingPutsOnStack() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShelteringLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Sheltering Light");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving grants indestructible to target creature and enters scry state")
    void resolvingGrantsIndestructibleAndScries() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShelteringLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.SCRY);
        assertThat(gd.interaction.scryContext()).isNotNull();
        assertThat(gd.interaction.scryContext().cards()).hasSize(1);
    }

    // ===== Scry 1 functionality =====

    @Test
    @DisplayName("Scry 1 keeping card on top preserves it")
    void scryKeepOnTop() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShelteringLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop = deck.get(0);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.getGameService().handleScryCompleted(gd, player1, List.of(0), List.of());

        assertThat(deck.get(0)).isSameAs(originalTop);
    }

    @Test
    @DisplayName("Scry 1 putting card on bottom moves it to bottom")
    void scryPutOnBottom() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShelteringLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop = deck.get(0);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.getGameService().handleScryCompleted(gd, player1, List.of(), List.of(0));

        assertThat(deck.get(0)).isNotSameAs(originalTop);
        assertThat(deck.get(deck.size() - 1)).isSameAs(originalTop);
    }

    // ===== End of turn cleanup =====

    @Test
    @DisplayName("Indestructible wears off at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShelteringLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.getGameService().handleScryCompleted(gd, player1, List.of(0), List.of());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Sheltering Light fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShelteringLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Sheltering Light"));
    }
}
