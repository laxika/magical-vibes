package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContractKillingTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has destroy target creature and create two treasure tokens effects")
    void hasCorrectEffects() {
        ContractKilling card = new ContractKilling();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(DestroyTargetPermanentEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(CreateTokenEffect.class);

        CreateTokenEffect treasureEffect = (CreateTokenEffect) card.getEffects(EffectSlot.SPELL).get(1);
        assertThat(treasureEffect.primaryType()).isEqualTo(CardType.ARTIFACT);
        assertThat(treasureEffect.amount()).isEqualTo(2);
    }

    // ===== Destroy + Treasure creation =====

    @Test
    @DisplayName("Destroys target creature and creates two Treasure tokens")
    void destroysCreatureAndCreatesTreasures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ContractKilling()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        // Target creature destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Two Treasure tokens created for the caster
        List<Permanent> treasures = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Treasure"))
                .toList();
        assertThat(treasures).hasSize(2);
        for (Permanent treasure : treasures) {
            assertThat(treasure.getCard().isToken()).isTrue();
            assertThat(treasure.getCard().getType()).isEqualTo(CardType.ARTIFACT);
            assertThat(treasure.getCard().getSubtypes()).containsExactly(CardSubtype.TREASURE);
            assertThat(treasure.getCard().getActivatedAbilities()).hasSize(1);
        }
    }

    @Test
    @DisplayName("Treasure tokens can be sacrificed for mana of any color")
    void treasureTokensProduceMana() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ContractKilling()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        // Two Treasure tokens on battlefield
        List<Permanent> treasures = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Treasure"))
                .toList();
        assertThat(treasures).hasSize(2);

        // Activate a Treasure token's ability (tap + sac → mana ability, resolves immediately)
        harness.activateAbility(player1, 0, null, null);

        // Treasure was sacrificed
        List<Permanent> remainingTreasures = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Treasure"))
                .toList();
        assertThat(remainingTreasures).hasSize(1);

        // Choose RED mana
        harness.handleListChoice(player1, "RED");

        // Red mana was added
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles when target creature is removed before resolution — no Treasure tokens created")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ContractKilling()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(targetId));

        harness.passBothPriorities();

        // Spell fizzles — no treasures
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Treasure"));
    }

    // ===== Targeting =====

    @Test
    @DisplayName("Cannot target non-creature permanents")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new Spellbook());
        UUID spellbookId = harness.getPermanentId(player2, "Spellbook");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ContractKilling()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, spellbookId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ContractKilling()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Contract Killing"));
    }
}
