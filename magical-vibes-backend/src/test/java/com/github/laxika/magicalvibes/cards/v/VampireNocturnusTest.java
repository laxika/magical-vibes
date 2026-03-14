package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.PlayWithTopCardRevealedEffect;
import com.github.laxika.magicalvibes.model.effect.TopCardOfLibraryColorConditionalEffect;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VampireNocturnusTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Vampire Nocturnus has correct card properties")
    void hasCorrectProperties() {
        VampireNocturnus card = new VampireNocturnus();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.STATIC).get(0)).isInstanceOf(PlayWithTopCardRevealedEffect.class);
        assertThat(card.getEffects(EffectSlot.STATIC).get(1)).isInstanceOf(TopCardOfLibraryColorConditionalEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Vampire Nocturnus puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new VampireNocturnus()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Vampire Nocturnus");
    }

    @Test
    @DisplayName("Resolving puts Vampire Nocturnus onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new VampireNocturnus()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Vampire Nocturnus"));
    }

    // ===== Conditional static effect: top card is black =====

    @Test
    @DisplayName("Buffs self when top card of library is black")
    void buffsSelfWhenTopCardIsBlack() {
        harness.addToBattlefield(player1, new VampireNocturnus());
        // Put a black card on top of library
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(new VampireAristocrat())));

        Permanent nocturnus = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Vampire Nocturnus"))
                .findFirst().orElseThrow();

        // 3/3 base + 2/1 from effect = 5/4
        assertThat(gqs.getEffectivePower(gd, nocturnus)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, nocturnus)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, nocturnus, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not buff self when top card of library is not black")
    void doesNotBuffSelfWhenTopCardIsNotBlack() {
        harness.addToBattlefield(player1, new VampireNocturnus());
        // Put a green card on top of library
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(new GrizzlyBears())));

        Permanent nocturnus = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Vampire Nocturnus"))
                .findFirst().orElseThrow();

        // 3/3 base, no bonus
        assertThat(gqs.getEffectivePower(gd, nocturnus)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, nocturnus)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, nocturnus, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Does not buff self when library is empty")
    void doesNotBuffSelfWhenLibraryIsEmpty() {
        harness.addToBattlefield(player1, new VampireNocturnus());
        gd.playerDecks.put(player1.getId(), new ArrayList<>());

        Permanent nocturnus = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Vampire Nocturnus"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, nocturnus)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, nocturnus)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, nocturnus, Keyword.FLYING)).isFalse();
    }

    // ===== Buffs other Vampires =====

    @Test
    @DisplayName("Buffs other Vampire creatures when top card is black")
    void buffsOtherVampiresWhenTopCardIsBlack() {
        harness.addToBattlefield(player1, new VampireNocturnus());
        harness.addToBattlefield(player1, new VampireAristocrat());
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(new VampireAristocrat())));

        Permanent aristocrat = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Vampire Aristocrat"))
                .findFirst().orElseThrow();

        // 2/2 base + 2/1 from Nocturnus = 4/3
        assertThat(gqs.getEffectivePower(gd, aristocrat)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, aristocrat)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, aristocrat, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not buff other Vampires when top card is not black")
    void doesNotBuffOtherVampiresWhenTopCardIsNotBlack() {
        harness.addToBattlefield(player1, new VampireNocturnus());
        harness.addToBattlefield(player1, new VampireAristocrat());
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(new GrizzlyBears())));

        Permanent aristocrat = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Vampire Aristocrat"))
                .findFirst().orElseThrow();

        // 2/2 base, no bonus
        assertThat(gqs.getEffectivePower(gd, aristocrat)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, aristocrat)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, aristocrat, Keyword.FLYING)).isFalse();
    }

    // ===== Does not buff non-Vampires =====

    @Test
    @DisplayName("Does not buff non-Vampire creatures even when top card is black")
    void doesNotBuffNonVampires() {
        harness.addToBattlefield(player1, new VampireNocturnus());
        harness.addToBattlefield(player1, new GrizzlyBears());
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(new VampireAristocrat())));

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // 2/2 base, no bonus (not a Vampire)
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    // ===== Does not buff opponent's Vampires =====

    @Test
    @DisplayName("Does not buff opponent's Vampire creatures")
    void doesNotBuffOpponentVampires() {
        harness.addToBattlefield(player1, new VampireNocturnus());
        harness.addToBattlefield(player2, new VampireAristocrat());
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(new VampireAristocrat())));

        Permanent opponentVampire = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Vampire Aristocrat"))
                .findFirst().orElseThrow();

        // 2/2 base, no bonus (opponent's creature, scope is OWN_CREATURES)
        assertThat(gqs.getEffectivePower(gd, opponentVampire)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentVampire)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentVampire, Keyword.FLYING)).isFalse();
    }

    // ===== Dynamic condition =====

    @Test
    @DisplayName("Buff toggles dynamically when top card changes")
    void buffTogglesDynamicallyWhenTopCardChanges() {
        harness.addToBattlefield(player1, new VampireNocturnus());
        harness.addToBattlefield(player1, new VampireAristocrat());

        // Start with a black card on top
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(new VampireAristocrat(), new GrizzlyBears())));

        Permanent aristocrat = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Vampire Aristocrat"))
                .findFirst().orElseThrow();
        Permanent nocturnus = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Vampire Nocturnus"))
                .findFirst().orElseThrow();

        // Black card on top: buff active
        assertThat(gqs.getEffectivePower(gd, aristocrat)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, nocturnus)).isEqualTo(5);

        // Remove top card (simulating draw), now green card is on top
        gd.playerDecks.get(player1.getId()).removeFirst();

        // Non-black card on top: buff inactive
        assertThat(gqs.getEffectivePower(gd, aristocrat)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, aristocrat)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, aristocrat, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, nocturnus)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, nocturnus)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, nocturnus, Keyword.FLYING)).isFalse();
    }

    // ===== Bonus removed when source leaves =====

    @Test
    @DisplayName("Bonus is removed when Vampire Nocturnus leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new VampireNocturnus());
        harness.addToBattlefield(player1, new VampireAristocrat());
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(new VampireAristocrat())));

        Permanent aristocrat = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Vampire Aristocrat"))
                .findFirst().orElseThrow();

        // Verify buff is applied
        assertThat(gqs.getEffectivePower(gd, aristocrat)).isEqualTo(4);

        // Remove Nocturnus
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Vampire Nocturnus"));

        // Bonus should be gone
        assertThat(gqs.getEffectivePower(gd, aristocrat)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, aristocrat)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, aristocrat, Keyword.FLYING)).isFalse();
    }

    // ===== Static bonus survives end-of-turn reset =====

    @Test
    @DisplayName("Static bonus survives end-of-turn modifier reset")
    void staticBonusSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new VampireNocturnus());
        harness.addToBattlefield(player1, new VampireAristocrat());
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(new VampireAristocrat())));

        Permanent aristocrat = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Vampire Aristocrat"))
                .findFirst().orElseThrow();

        // Add temporary boost
        aristocrat.setPowerModifier(aristocrat.getPowerModifier() + 5);
        assertThat(gqs.getEffectivePower(gd, aristocrat)).isEqualTo(9); // 2 base + 5 spell + 2 static

        // Reset end-of-turn modifiers
        aristocrat.resetModifiers();

        // Static bonus still applied
        assertThat(gqs.getEffectivePower(gd, aristocrat)).isEqualTo(4); // 2 base + 2 static
        assertThat(gqs.getEffectiveToughness(gd, aristocrat)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, aristocrat, Keyword.FLYING)).isTrue();
    }

    // ===== Self-buff regardless of creature type (CR 201.5) =====

    @Test
    @DisplayName("Buffs self even if Vampire subtype is removed (card names itself)")
    void buffsSelfEvenIfVampireSubtypeRemoved() {
        VampireNocturnus nocturnusCard = new VampireNocturnus();
        // Remove the Vampire subtype to simulate type-changing effects
        nocturnusCard.setSubtypes(List.of());
        harness.addToBattlefield(player1, nocturnusCard);
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(new VampireAristocrat())));

        Permanent nocturnus = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Vampire Nocturnus"))
                .findFirst().orElseThrow();

        // Per rulings: Vampire Nocturnus always buffs itself when condition is met,
        // even if it's no longer a Vampire (the card names itself in its text)
        assertThat(gqs.getEffectivePower(gd, nocturnus)).isEqualTo(5); // 3 base + 2
        assertThat(gqs.getEffectiveToughness(gd, nocturnus)).isEqualTo(4); // 3 base + 1
        assertThat(gqs.hasKeyword(gd, nocturnus, Keyword.FLYING)).isTrue();
    }
}
