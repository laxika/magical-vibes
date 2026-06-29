package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.MassFightTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlphaBrawlTest extends BaseCardTest {

    @Test
    @DisplayName("Alpha Brawl has MassFightTargetCreatureEffect")
    void hasCorrectEffect() {
        AlphaBrawl card = new AlphaBrawl();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(MassFightTargetCreatureEffect.class);
    }

    @Test
    @DisplayName("Target creature deals power damage to each other creature, then they deal back")
    void targetDealsDamageToOthersAndViceVersa() {
        // Opponent controls Hill Giant (3/3), Grizzly Bears (2/2), and Llanowar Elves (1/1)
        // Target Hill Giant:
        //   Step 1: Hill Giant deals 3 to Bears (lethal) and 3 to Elves (lethal)
        //   Step 2: Bears deal 2 to Hill Giant, Elves deal 1 to Hill Giant = 3 total to Hill Giant (lethal)
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new AlphaBrawl()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        UUID hillGiantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castSorcery(player1, 0, hillGiantId);
        harness.passBothPriorities();

        // All three creatures should die
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Target creature survives if total power of others is less than its toughness")
    void targetSurvivesWhenDamageInsufficient() {
        // Opponent controls Hill Giant (3/3) and Llanowar Elves (1/1)
        // Target Hill Giant:
        //   Step 1: Hill Giant deals 3 to Elves (lethal)
        //   Step 2: Elves deal 1 to Hill Giant (survives with 1 damage marked)
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new AlphaBrawl()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        UUID hillGiantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castSorcery(player1, 0, hillGiantId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");

        Permanent hillGiant = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Hill Giant"))
                .findFirst().orElseThrow();
        assertThat(hillGiant.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does nothing when target is the only creature opponent controls")
    void doesNothingWithSingleCreature() {
        // Opponent controls only Hill Giant (3/3)
        // No other creatures to deal damage to or receive damage from
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new AlphaBrawl()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        UUID hillGiantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castSorcery(player1, 0, hillGiantId);
        harness.passBothPriorities();

        // Hill Giant should survive undamaged
        harness.assertOnBattlefield(player2, "Hill Giant");
        Permanent hillGiant = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(hillGiant.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot target own creature")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AlphaBrawl()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        UUID ownGiantId = harness.getPermanentId(player1, "Hill Giant");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, ownGiantId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Spell fizzles when target is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AlphaBrawl()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        UUID hillGiantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castSorcery(player1, 0, hillGiantId);

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(hillGiantId));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        // Other creature should be unharmed
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Small target creature still takes damage from all others")
    void smallTargetTakesMassiveDamage() {
        // Opponent controls Llanowar Elves (1/1), Hill Giant (3/3), and Grizzly Bears (2/2)
        // Target Llanowar Elves:
        //   Step 1: Elves deal 1 to Hill Giant (survives) and 1 to Bears (survives)
        //   Step 2: Hill Giant deals 3 to Elves and Bears deal 2 to Elves = 5 total (very lethal)
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AlphaBrawl()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castSorcery(player1, 0, elvesId);
        harness.passBothPriorities();

        // Elves die from damage dealt by Hill Giant and Bears
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
        // Hill Giant and Bears survive with 1 damage each
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        Permanent hillGiant = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Hill Giant"))
                .findFirst().orElseThrow();
        assertThat(hillGiant.getMarkedDamage()).isEqualTo(1);

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not affect caster's creatures")
    void doesNotAffectCasterCreatures() {
        // Player 1 has a creature, player 2 has two creatures
        // Alpha Brawl should only affect player 2's creatures
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new AlphaBrawl()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, bearsId);
        harness.passBothPriorities();

        // Player 1's Hill Giant should be completely unaffected
        harness.assertOnBattlefield(player1, "Hill Giant");
        Permanent hillGiant = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(hillGiant.getMarkedDamage()).isZero();
    }
}
