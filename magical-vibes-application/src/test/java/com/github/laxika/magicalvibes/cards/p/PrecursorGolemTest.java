package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherSubtypePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PrecursorGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Card has ETB token effect and spell-copy trigger")
    void hasCorrectEffects() {
        PrecursorGolem card = new PrecursorGolem();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(CreateTokenEffect.class);
        CreateTokenEffect tokenEffect =
                (CreateTokenEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(tokenEffect.amount()).isEqualTo(2);
        assertThat(tokenEffect.tokenName()).isEqualTo("Golem");
        assertThat(tokenEffect.power()).isEqualTo(3);
        assertThat(tokenEffect.toughness()).isEqualTo(3);
        assertThat(tokenEffect.subtypes()).containsExactly(CardSubtype.GOLEM);
        assertThat(tokenEffect.additionalTypes()).containsExactly(CardType.ARTIFACT);

        assertThat(card.getEffects(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL).getFirst())
                .isInstanceOf(CopySpellForEachOtherSubtypePermanentEffect.class);
    }

    @Test
    @DisplayName("ETB creates two 3/3 colorless Golem artifact creature tokens")
    void etbCreatesTwoGolemTokens() {
        castAndResolveGolemWithTokens();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(3); // Precursor Golem + 2 tokens
        assertThat(countGolemTokens(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting instant targeting a Golem triggers Precursor Golem's ability")
    void spellTargetingGolemTriggers() {
        castAndResolveGolemWithTokens();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID golemTokenId = getAnyGolemTokenId(player1);

        harness.castInstant(player1, 0, golemTokenId);

        // Stack: Shock (bottom) + triggered ability from Precursor Golem (top)
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shock");
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Precursor Golem");
    }

    @Test
    @DisplayName("Triggered ability creates copies targeting each other Golem")
    void triggeredAbilityCreatesCopies() {
        castAndResolveGolemWithTokens();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID golemTokenId = getAnyGolemTokenId(player1);

        harness.castInstant(player1, 0, golemTokenId);
        harness.passBothPriorities(); // resolve triggered ability → creates 2 copies

        // Stack: original Shock (bottom) + 2 copies (top)
        assertThat(gd.stack).hasSize(3);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shock");
        assertThat(gd.stack.get(1).isCopy()).isTrue();
        assertThat(gd.stack.get(1).getCard().getName()).isEqualTo("Shock");
        assertThat(gd.stack.get(2).isCopy()).isTrue();
        assertThat(gd.stack.get(2).getCard().getName()).isEqualTo("Shock");
    }

    @Test
    @DisplayName("All copies and original Shock resolve — all Golems survive 2 damage on 3/3")
    void allCopiesAndOriginalResolve() {
        castAndResolveGolemWithTokens();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID golemTokenId = getAnyGolemTokenId(player1);

        harness.castInstant(player1, 0, golemTokenId);
        harness.passBothPriorities(); // resolve triggered ability
        harness.passBothPriorities(); // resolve first copy
        harness.passBothPriorities(); // resolve second copy
        harness.passBothPriorities(); // resolve original Shock

        assertThat(gd.stack).isEmpty();
        // All 3 Golems survive (2 damage < 3 toughness)
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Opponent casting a spell targeting a Golem also triggers copies")
    void opponentSpellTargetingGolemTriggersCopies() {
        castAndResolveGolemWithTokens();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID golemTokenId = getAnyGolemTokenId(player1);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, golemTokenId);

        // Triggered ability fires for opponent's spell too
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("No trigger when spell targets a player")
    void noTriggerWhenTargetingPlayer() {
        castAndResolveGolemWithTokens();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shock");
    }

    @Test
    @DisplayName("No trigger when spell targets a non-Golem creature")
    void noTriggerWhenTargetingNonGolem() {
        castAndResolveGolemWithTokens();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castInstant(player1, 0, bearsId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shock");
    }

    @Test
    @DisplayName("Each copy targets a different Golem")
    void eachCopyTargetsDifferentGolem() {
        castAndResolveGolemWithTokens();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID golemTokenId = getAnyGolemTokenId(player1);

        harness.castInstant(player1, 0, golemTokenId);
        harness.passBothPriorities(); // resolve triggered ability

        // Collect all target IDs
        List<UUID> allTargets = gd.stack.stream()
                .map(se -> se.getTargetId())
                .toList();

        // All 3 Golems should be targeted (original + 2 copies)
        assertThat(allTargets).hasSize(3);
        assertThat(allTargets).doesNotHaveDuplicates();
    }

    private void castAndResolveGolemWithTokens() {
        harness.setHand(player1, List.of(new PrecursorGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger
    }

    private int countGolemTokens(Player player) {
        return (int) gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Golem"))
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.GOLEM))
                .count();
    }

    private UUID getAnyGolemTokenId(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Golem"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No Golem token found"))
                .getId();
    }
}
