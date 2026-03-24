package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HuatlisSnubhorn;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.FirstTargetFightsSecondTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnFirstTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfTargetingControlledSubtypeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SavageStompTest extends BaseCardTest {

    @Test
    @DisplayName("Has correct effects: cost reduction, +1/+1 counter on first target, fight")
    void hasCorrectEffects() {
        SavageStomp card = new SavageStomp();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(ReduceOwnCastCostIfTargetingControlledSubtypeEffect.class);

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0))
                .isInstanceOf(PutPlusOnePlusOneCounterOnFirstTargetEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1))
                .isInstanceOf(FirstTargetFightsSecondTargetEffect.class);
    }

    @Test
    @DisplayName("Creature gets +1/+1 counter before fighting — counter helps survive")
    void counterBeforeFight() {
        // Grizzly Bears (2/2) gets +1/+1 counter -> 3/3, fights Llanowar Elves (1/1)
        // Bears deal 3 (lethal), Elves deal 1 (Bears survive at 3/3 with 1 damage)
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new SavageStomp()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castSorcery(player1, 0, List.of(bearId, elvesId));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("+1/+1 counter lets creature trade instead of losing — both die at equal stats")
    void counterHelpsCreatureTradeUpInFight() {
        // Grizzly Bears (2/2) fights Hill Giant (3/3)
        // Without counter: Bears deal 2 (Giant survives), Giant deals 3 (Bears die) — Bears lose
        // With counter: Bears become 3/3, deal 3 (Giant dies), Giant deals 3 (Bears die) — trade
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new SavageStomp()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castSorcery(player1, 0, List.of(bearId, giantId));
        harness.passBothPriorities();

        // Both die: Bears 3/3 takes 3 damage, Giant 3/3 takes 3 damage
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Costs {G} when targeting a Dinosaur you control")
    void costReductionWhenTargetingDinosaur() {
        // Huatli's Snubhorn is a Dinosaur — cost should be reduced from {2}{G} to {G}
        harness.addToBattlefield(player1, new HuatlisSnubhorn());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new SavageStomp()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID dinoId = harness.getPermanentId(player1, "Huatli's Snubhorn");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castSorcery(player1, 0, List.of(dinoId, elvesId));
        harness.passBothPriorities();

        // Dino (2/2 + counter -> 3/3) fights Elves (1/1) — Elves die
        harness.assertOnBattlefield(player1, "Huatli's Snubhorn");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Costs full {2}{G} when targeting a non-Dinosaur you control")
    void fullCostWhenTargetingNonDinosaur() {
        // Grizzly Bears is not a Dinosaur — full cost {2}{G} required
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new SavageStomp()));
        // Only provide 1 green mana — not enough for full cost {2}{G}
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bearId, elvesId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Full cost works when targeting a non-Dinosaur with enough mana")
    void fullCostNonDinosaurWithEnoughMana() {
        // Control a Dinosaur but target a non-Dinosaur — must pay full cost
        harness.addToBattlefield(player1, new HuatlisSnubhorn());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new SavageStomp()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castSorcery(player1, 0, List.of(bearId, elvesId));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cannot afford when targeting non-Dinosaur with only reduced-cost mana while controlling a Dinosaur")
    void cannotAffordNonDinoTargetWithReducedManaEvenIfControllingDino() {
        // Control a Dinosaur but target the non-Dinosaur — only 1G available,
        // but since first target is non-Dinosaur, full cost {2}{G} applies
        harness.addToBattlefield(player1, new HuatlisSnubhorn());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new SavageStomp()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bearId, elvesId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target opponent's creature as first target")
    void cannotTargetOpponentCreatureAsFirstTarget() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new SavageStomp()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID theirBearId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID theirElvesId = harness.getPermanentId(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(theirBearId, theirElvesId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Cannot target own creature as second target")
    void cannotTargetOwnCreatureAsSecondTarget() {
        GrizzlyBears bear1 = new GrizzlyBears();
        GrizzlyBears bear2 = new GrizzlyBears();
        harness.addToBattlefield(player1, bear1);
        harness.addToBattlefield(player1, bear2);
        harness.setHand(player1, List.of(new SavageStomp()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        UUID id1 = bf.get(0).getId();
        UUID id2 = bf.get(1).getId();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(id1, id2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Neither creature fights when first target removed before resolution (701.14b)")
    void neitherFightsWhenFirstTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new SavageStomp()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castSorcery(player1, 0, List.of(bearId, elvesId));

        // Remove first target before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        // Llanowar Elves should survive — neither creature fights per 701.14b
        harness.assertOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Dinosaur counter is applied even with reduced cost")
    void dinosaurGetsCounterWithReducedCost() {
        // Huatli's Snubhorn (2/2 Dinosaur) with reduced cost {G}
        harness.addToBattlefield(player1, new HuatlisSnubhorn());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SavageStomp()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID dinoId = harness.getPermanentId(player1, "Huatli's Snubhorn");
        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, List.of(dinoId, bearId));
        harness.passBothPriorities();

        // Dino (2/2 + counter -> 3/3) fights Bears (2/2) — Bears die, Dino survives at 1
        harness.assertOnBattlefield(player1, "Huatli's Snubhorn");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");

        Permanent dino = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(dino.getPlusOnePlusOneCounters()).isEqualTo(1);
    }
}
