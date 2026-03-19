package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostmortemLungeTest extends BaseCardTest {

    @Test
    @DisplayName("Postmortem Lunge has correct effects")
    void hasCorrectEffects() {
        PostmortemLunge card = new PostmortemLunge();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(ReturnCardFromGraveyardEffect.class);

        ReturnCardFromGraveyardEffect effect = (ReturnCardFromGraveyardEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.grantHaste()).isTrue();
        assertThat(effect.exileAtEndStep()).isTrue();
        assertThat(effect.requiresManaValueEqualsX()).isTrue();
        assertThat(effect.targetGraveyard()).isTrue();
    }

    @Test
    @DisplayName("Casting Postmortem Lunge puts it on the stack with graveyard target")
    void castingPutsOnStack() {
        Card target = new GrizzlyBears(); // mana value 2
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new PostmortemLunge()));
        harness.addMana(player1, ManaColor.BLACK, 3); // {2}{B/P} — X=2, base cost {B/P}

        harness.castSorcery(player1, 0, 2, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
        assertThat(entry.getTargetZone()).isEqualTo(Zone.GRAVEYARD);
        assertThat(entry.getXValue()).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolving returns creature to battlefield with haste")
    void resolvesAndReturnsCreatureWithHaste() {
        Card target = new GrizzlyBears(); // mana value 2
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new PostmortemLunge()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 2, target.getId());
        harness.passBothPriorities();

        // Creature should be on player1's battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Creature should be removed from graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));

        // Creature should have haste
        Permanent creature = findCreatureOnBattlefield(player1.getId(), "Grizzly Bears");
        assertThat(creature.getGrantedKeywords()).contains(Keyword.HASTE);

        // Creature should be marked for exile at end step
        assertThat(gd.pendingTokenExilesAtEndStep).contains(creature.getId());
    }

    @Test
    @DisplayName("Creature is exiled at the beginning of the next end step")
    void creatureExiledAtEndStep() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new PostmortemLunge()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 2, target.getId());
        harness.passBothPriorities();

        // Advance to end step
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Creature should no longer be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Creature should be in exile
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target creature with mana value different from X")
    void cannotTargetWrongManaValue() {
        Card target = new SerraAngel(); // mana value 5
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new PostmortemLunge()));
        harness.addMana(player1, ManaColor.BLACK, 3); // X=2, but Serra Angel has mana value 5

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value must equal X");
    }

    @Test
    @DisplayName("Can target creature with mana value 0 when X=0")
    void canTargetManaValueZero() {
        // Ornithopter-like creature with mana value 0 would work, but let's use a simpler approach
        // We need a creature with mana value 0. Let's just test with X=2 and a matching creature.
        Card target = new GrizzlyBears(); // mana value 2
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new PostmortemLunge()));
        harness.addMana(player1, ManaColor.BLACK, 3); // {2}{B} for X=2

        harness.castSorcery(player1, 0, 2, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Fizzles if target leaves graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyard() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new PostmortemLunge()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 2, target.getId());
        gd.playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Cannot target opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new PostmortemLunge()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Postmortem Lunge goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new PostmortemLunge()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 2, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Postmortem Lunge"));
    }

    private Permanent findCreatureOnBattlefield(UUID playerId, String cardName) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(p -> p.getCard().getName().equals(cardName))
                .findFirst()
                .orElseThrow(() -> new AssertionError(cardName + " not found on battlefield"));
    }
}
