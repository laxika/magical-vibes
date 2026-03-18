package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RollingTemblorTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has correct effect configuration")
    void hasCorrectEffect() {
        RollingTemblor card = new RollingTemblor();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(MassDamageEffect.class);
        MassDamageEffect effect = (MassDamageEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.damage()).isEqualTo(2);
        assertThat(effect.damagesPlayers()).isFalse();
        assertThat(effect.filter()).isInstanceOf(PermanentNotPredicate.class);
        PermanentNotPredicate notPred = (PermanentNotPredicate) effect.filter();
        assertThat(notPred.predicate()).isInstanceOf(PermanentHasKeywordPredicate.class);
        PermanentHasKeywordPredicate keywordPred = (PermanentHasKeywordPredicate) notPred.predicate();
        assertThat(keywordPred.keyword()).isEqualTo(Keyword.FLYING);

        FlashbackCast flashback = card.getCastingOption(FlashbackCast.class).orElseThrow();
        assertThat(flashback.getCost(ManaCastingCost.class).orElseThrow().manaCost()).isEqualTo("{4}{R}{R}");
    }

    // ===== Normal cast =====

    @Test
    @DisplayName("Kills creatures without flying on both sides")
    void killsCreaturesWithoutFlying() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RollingTemblor()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Does not damage creatures with flying")
    void doesNotDamageCreaturesWithFlying() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new RollingTemblor()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Air Elemental"));
    }

    @Test
    @DisplayName("Damages ground creatures but leaves flyers unharmed")
    void selectivelyDamages() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RollingTemblor()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Air Elemental"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Does not deal damage to players")
    void doesNotDamagePlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new RollingTemblor()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Goes to graveyard after normal cast resolves")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new RollingTemblor()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Rolling Temblor"));
    }

    // ===== Flashback =====

    @Test
    @DisplayName("Flashback kills creatures without flying")
    void flashbackKillsGroundCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setGraveyard(player1, List.of(new RollingTemblor()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Air Elemental"));
    }

    @Test
    @DisplayName("Flashback spell is exiled after resolving")
    void flashbackExilesAfterResolving() {
        harness.setGraveyard(player1, List.of(new RollingTemblor()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Rolling Temblor"));
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Rolling Temblor"));
    }

    @Test
    @DisplayName("Flashback puts spell on stack with flashback flag")
    void flashbackPutsOnStack() {
        harness.setGraveyard(player1, List.of(new RollingTemblor()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castFlashback(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Rolling Temblor");
        assertThat(entry.isCastWithFlashback()).isTrue();
    }

    @Test
    @DisplayName("Cannot cast flashback without enough mana")
    void flashbackFailsWithoutMana() {
        harness.setGraveyard(player1, List.of(new RollingTemblor()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
