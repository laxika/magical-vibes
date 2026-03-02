package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GruesomeEncoreTest extends BaseCardTest {

    @Test
    @DisplayName("Gruesome Encore has correct effects")
    void hasCorrectEffects() {
        GruesomeEncore card = new GruesomeEncore();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect.class);
    }

    @Test
    @DisplayName("Casting Gruesome Encore puts it on the stack with graveyard target")
    void castingPutsOnStack() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new GruesomeEncore()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetPermanentId()).isEqualTo(target.getId());
        assertThat(entry.getTargetZone()).isEqualTo(Zone.GRAVEYARD);
    }

    @Test
    @DisplayName("Resolving puts creature onto battlefield with haste under caster's control")
    void resolvesAndPutsCreatureOnBattlefield() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new GruesomeEncore()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        // Creature should be on player1's battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Creature should be removed from player2's graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));

        // Creature should have haste
        Permanent creature = findCreatureOnBattlefield(player1.getId(), "Grizzly Bears");
        assertThat(creature.getGrantedKeywords()).contains(Keyword.HASTE);

        // Creature should be marked for exile at end step
        assertThat(gd.pendingTokenExilesAtEndStep).contains(creature.getId());

        // Creature should be tracked as stolen
        assertThat(gd.stolenCreatures).containsKey(creature.getId());

        // Creature should be marked with exile-if-leaves replacement
        assertThat(creature.isExileIfLeavesBattlefield()).isTrue();
    }

    @Test
    @DisplayName("Creature is exiled at end step")
    void creatureExiledAtEndStep() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new GruesomeEncore()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        // Advance to end step to trigger exile (must force to POSTCOMBAT_MAIN so
        // passBothPriorities naturally advances to END_STEP, firing handleEndStepTriggers)
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Creature should no longer be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Creature should be in exile (original owner's)
        assertThat(gd.playerExiledCards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Creature is exiled instead of going to graveyard when destroyed by damage")
    void exileReplacementWhenDestroyed() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new GruesomeEncore()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        Permanent creature = findCreatureOnBattlefield(player1.getId(), "Grizzly Bears");
        UUID creatureId = creature.getId();

        // Reset game state so player2 can cast an instant
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Use Shock to deal 2 damage and destroy Grizzly Bears (2 toughness)
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, creatureId);

        // Creature should NOT be in any graveyard (replacement redirects to exile)
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        // Creature should be in exile (original owner's)
        assertThat(gd.playerExiledCards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Creature is exiled instead of returning to hand when bounced")
    void exileReplacementWhenBounced() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new GruesomeEncore()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        Permanent creature = findCreatureOnBattlefield(player1.getId(), "Grizzly Bears");
        UUID creatureId = creature.getId();

        // Reset game state so player2 can cast an instant
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Use Unsummon to bounce the creature
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, creatureId);

        // Creature should NOT be in any hand (replacement redirects to exile)
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        // Creature should be in exile (original owner's)
        assertThat(gd.playerExiledCards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target creature card in your own graveyard")
    void cannotTargetOwnGraveyard() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new GruesomeEncore()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent's graveyard");
    }

    @Test
    @DisplayName("Fizzles if target leaves graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyard() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new GruesomeEncore()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, target.getId());
        gd.playerGraveyards.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Cannot target non-creature card in opponent's graveyard")
    void cannotTargetNonCreature() {
        Card enchantment = new Pacifism();
        harness.setGraveyard(player2, List.of(enchantment));
        harness.setHand(player1, List.of(new GruesomeEncore()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature card");
    }

    private Permanent findCreatureOnBattlefield(UUID playerId, String cardName) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(p -> p.getCard().getName().equals(cardName))
                .findFirst()
                .orElseThrow(() -> new AssertionError(cardName + " not found on battlefield"));
    }
}
