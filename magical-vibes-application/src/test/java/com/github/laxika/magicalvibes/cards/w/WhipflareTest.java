package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class WhipflareTest extends BaseCardTest {

    @Test
    @DisplayName("Whipflare has correct effect configuration")
    void hasCorrectEffect() {
        Whipflare card = new Whipflare();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(MassDamageEffect.class);
        MassDamageEffect effect = (MassDamageEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.damage()).isEqualTo(2);
        assertThat(effect.damagesPlayers()).isFalse();
        assertThat(effect.filter()).isInstanceOf(PermanentNotPredicate.class);
        PermanentNotPredicate notPred = (PermanentNotPredicate) effect.filter();
        assertThat(notPred.predicate()).isInstanceOf(PermanentIsArtifactPredicate.class);
    }

    @Test
    @DisplayName("Whipflare kills nonartifact creatures with toughness 2 or less on both sides")
    void killsNonArtifactCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Whipflare()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Whipflare does not damage artifact creatures")
    void doesNotDamageArtifactCreatures() {
        Card artifactCreature = makeArtifactCreature("Myr Sire", 1, 1);
        harness.addToBattlefield(player2, artifactCreature);
        harness.setHand(player1, List.of(new Whipflare()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Myr Sire"));
    }

    @Test
    @DisplayName("Whipflare damages nonartifact creatures but leaves artifact creatures unharmed")
    void selectivelyDamages() {
        Card artifactCreature = makeArtifactCreature("Myr Sire", 1, 1);
        harness.addToBattlefield(player2, artifactCreature);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Whipflare()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Artifact creature survives
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Myr Sire"));
        // Nonartifact creature dies
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Whipflare does not deal damage to players")
    void doesNotDamagePlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Whipflare()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== Helpers =====

    private Card makeArtifactCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setAdditionalTypes(Set.of(CardType.ARTIFACT));
        card.setManaCost("{1}");
        card.setColor(null);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
