package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DestructiveForceTest extends BaseCardTest {

    private static Card bigCreature() {
        Card card = new Card();
        card.setName("Big Creature");
        card.setType(CardType.CREATURE);
        card.setManaCost("{4}{G}{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(6);
        card.setToughness(6);
        return card;
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Destructive Force has sacrifice-lands and mass-damage effects")
    void hasCorrectEffects() {
        DestructiveForce card = new DestructiveForce();

        List<?> effects = card.getEffects(EffectSlot.SPELL);
        assertThat(effects).hasSize(2);
        assertThat(effects.get(0)).isInstanceOf(EachPlayerSacrificesPermanentsEffect.class);
        assertThat(effects.get(1)).isInstanceOf(MassDamageEffect.class);

        EachPlayerSacrificesPermanentsEffect sacrificeEffect = (EachPlayerSacrificesPermanentsEffect) effects.get(0);
        assertThat(sacrificeEffect.count()).isEqualTo(5);

        MassDamageEffect damageEffect = (MassDamageEffect) effects.get(1);
        assertThat(damageEffect.damage()).isEqualTo(5);
    }

    // ===== Sacrifice lands =====

    @Test
    @DisplayName("Both players with 5 or fewer lands lose all their lands")
    void bothPlayersLoseAllLandsWhenFiveOrFewer() {
        // Player1 has 3 lands, Player2 has 2 lands
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new Mountain());
        }
        for (int i = 0; i < 2; i++) {
            harness.addToBattlefield(player2, new Forest());
        }

        harness.setHand(player1, List.of(new DestructiveForce()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // All lands should be sacrificed (both players had ≤5)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Mountain"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Forest"));
    }

    @Test
    @DisplayName("Player with more than 5 lands is prompted to choose which 5 to sacrifice")
    void playerWithMoreThanFiveLandsPromptsChoice() {
        // Player1 has 7 lands
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new Mountain());
        }
        // Player2 has 2 lands (will be auto-marked for sacrifice)
        for (int i = 0; i < 2; i++) {
            harness.addToBattlefield(player2, new Forest());
        }

        harness.setHand(player1, List.of(new DestructiveForce()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Player1 (active player, APNAP first) must choose 5 of 7 lands.
        // Player2's lands are deferred — all sacrifices happen simultaneously per ruling.
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_PERMANENT_CHOICE);
        assertThat(gd.pendingForcedSacrificeCount).isEqualTo(5);
        assertThat(gd.pendingForcedSacrificePlayerId).isEqualTo(player1.getId());

        // Player2's lands are still on the battlefield (deferred for simultaneous sacrifice)
        assertThat(gd.pendingSimultaneousSacrificeIds).hasSize(2);

        // Player1 chooses 5 lands
        List<Permanent> p1Lands = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Mountain"))
                .toList();
        List<UUID> chosen = p1Lands.stream().limit(5).map(Permanent::getId).toList();
        harness.handleMultiplePermanentsChosen(player1, chosen);

        // After choice, ALL lands are sacrificed simultaneously
        // Player1 should have exactly 2 lands remaining
        long p1Remaining = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Mountain"))
                .count();
        assertThat(p1Remaining).isEqualTo(2);

        // Player2's lands should now be gone too
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Forest"));
    }

    @Test
    @DisplayName("Both players with more than 5 lands are prompted sequentially, sacrificed simultaneously")
    void bothPlayersWithMoreThanFiveLandsPromptedSequentially() {
        // Player1 has 7 lands, Player2 has 6 lands
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new Mountain());
        }
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player2, new Forest());
        }

        harness.setHand(player1, List.of(new DestructiveForce()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // First player in APNAP order is prompted (active player = player1)
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_PERMANENT_CHOICE);
        assertThat(gd.pendingForcedSacrificePlayerId).isEqualTo(player1.getId());

        // First player chooses 5 lands — no sacrifice happens yet (deferred)
        List<Permanent> p1Lands = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .toList();
        List<UUID> firstChosen = p1Lands.stream().limit(5).map(Permanent::getId).toList();
        harness.handleMultiplePermanentsChosen(player1, firstChosen);

        // All 7 of player1's lands are still on the battlefield (sacrifice is deferred)
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND)).count()).isEqualTo(7);

        // Second player should now be prompted
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_PERMANENT_CHOICE);
        assertThat(gd.pendingForcedSacrificePlayerId).isEqualTo(player2.getId());

        // Second player chooses 5 lands — now ALL chosen lands are sacrificed simultaneously
        List<Permanent> p2Lands = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .toList();
        List<UUID> secondChosen = p2Lands.stream().limit(5).map(Permanent::getId).toList();
        harness.handleMultiplePermanentsChosen(player2, secondChosen);

        // Both players should have remaining lands after simultaneous sacrifice
        long p1Remaining = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND)).count();
        long p2Remaining = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND)).count();
        assertThat(p1Remaining).isEqualTo(2);
        assertThat(p2Remaining).isEqualTo(1);
    }

    @Test
    @DisplayName("Player with no lands is unaffected by the sacrifice part")
    void playerWithNoLandsIsUnaffected() {
        harness.addToBattlefield(player1, new Mountain());
        // Player2 has no lands, only a creature
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new DestructiveForce()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Player1's land is sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Mountain"));

        // Player2's creature is killed by the 5 damage, but no lands to sacrifice
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Damage to creatures =====

    @Test
    @DisplayName("Deals 5 damage to each creature, killing those with toughness 5 or less")
    void killsCreaturesWithToughnessFiveOrLess() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2

        harness.setHand(player1, List.of(new DestructiveForce()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Creatures with toughness greater than 5 survive the damage")
    void creaturesWithHighToughnessSurvive() {
        harness.addToBattlefield(player2, bigCreature()); // 6/6

        harness.setHand(player1, List.of(new DestructiveForce()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Big Creature (6/6) survives with 5 damage marked
        harness.assertOnBattlefield(player2, "Big Creature");
    }

    @Test
    @DisplayName("Does not deal damage to players")
    void doesNotDealDamageToPlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new DestructiveForce()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== Combined behavior =====

    @Test
    @DisplayName("Non-land permanents (creatures) are not sacrificed, only damaged")
    void nonLandPermanentsAreNotSacrificedOnlyDamaged() {
        harness.addToBattlefield(player1, bigCreature()); // 6/6
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());

        harness.setHand(player1, List.of(new DestructiveForce()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Mountains are sacrificed (both, since <5)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Mountain"));

        // Big Creature survives — not sacrificed and survives 5 damage (6 toughness)
        harness.assertOnBattlefield(player1, "Big Creature");
    }
}
