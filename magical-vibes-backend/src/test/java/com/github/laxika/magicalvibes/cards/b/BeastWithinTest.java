package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BeastWithinTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has destroy target permanent and create token effect")
    void hasCorrectEffect() {
        BeastWithin card = new BeastWithin();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0))
                .isInstanceOf(DestroyTargetPermanentEffect.class);
        DestroyTargetPermanentEffect effect = (DestroyTargetPermanentEffect) card.getEffects(EffectSlot.SPELL).get(0);
        assertThat(effect.tokenForController()).isNotNull();
    }

    // ===== Destroy creature and give opponent a Beast token =====

    @Test
    @DisplayName("Destroys target creature and gives its controller a 3/3 Beast token")
    void destroysCreatureAndCreatesTokenForController() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BeastWithin()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        // Target creature destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Opponent gets a 3/3 green Beast token
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Beast")
                        && p.getCard().isToken()
                        && p.getCard().getType() == CardType.CREATURE
                        && p.getCard().getPower() == 3
                        && p.getCard().getToughness() == 3
                        && p.getCard().getSubtypes().contains(CardSubtype.BEAST));
    }

    // ===== Destroy own permanent =====

    @Test
    @DisplayName("Can target own permanent — controller gets the Beast token")
    void canDestroyOwnPermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BeastWithin()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        // Own creature destroyed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Controller (player1) gets a 3/3 Beast token
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Beast")
                        && p.getCard().isToken()
                        && p.getCard().getPower() == 3
                        && p.getCard().getToughness() == 3);
    }

    // ===== Destroy non-creature permanents =====

    @Test
    @DisplayName("Can destroy a land — target permanent is any permanent")
    void canDestroyLand() {
        harness.addToBattlefield(player2, new Forest());
        UUID targetId = harness.getPermanentId(player2, "Forest");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BeastWithin()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        // Land destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Forest"));

        // Opponent gets a Beast token
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Beast") && p.getCard().isToken());
    }

    @Test
    @DisplayName("Can destroy an artifact")
    void canDestroyArtifact() {
        harness.addToBattlefield(player2, new Spellbook());
        UUID targetId = harness.getPermanentId(player2, "Spellbook");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BeastWithin()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        // Artifact destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Spellbook"));

        // Opponent gets a Beast token
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Beast") && p.getCard().isToken());
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles when target is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BeastWithin()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(targetId));

        harness.passBothPriorities();

        // Spell fizzles — no Beast token created
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Beast"));
    }

    // ===== Goes to graveyard =====

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BeastWithin()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Beast Within"));
    }
}
