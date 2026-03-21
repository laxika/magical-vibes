package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SettleTheScoreTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has two SPELL effects: ExileTargetPermanentEffect and PutCounterOnTargetPermanentEffect")
    void hasCorrectEffects() {
        SettleTheScore card = new SettleTheScore();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(ExileTargetPermanentEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(PutCounterOnTargetPermanentEffect.class);

        PutCounterOnTargetPermanentEffect counterEffect =
                (PutCounterOnTargetPermanentEffect) card.getEffects(EffectSlot.SPELL).get(1);
        assertThat(counterEffect.counterType()).isEqualTo(CounterType.LOYALTY);
        assertThat(counterEffect.count()).isEqualTo(2);
        assertThat(counterEffect.predicate()).isNotNull();
    }

    @Test
    @DisplayName("Targets creatures")
    void targetsCreatures() {
        SettleTheScore card = new SettleTheScore();
        assertThat(card.isNeedsTarget()).isTrue();
    }

    // ===== Exile target creature =====

    @Test
    @DisplayName("Exiles target creature")
    void exilesTargetCreature() {
        Permanent creature = addReadyCreature(player2);

        harness.setHand(player1, List.of(new SettleTheScore()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isNotEmpty();
    }

    // ===== Loyalty counters on planeswalker =====

    @Test
    @DisplayName("Puts two loyalty counters on own planeswalker when exiling creature")
    void putsTwoLoyaltyCountersOnPlaneswalker() {
        Permanent creature = addReadyCreature(player2);
        Permanent planeswalker = addReadyPlaneswalker(player1, 3);

        harness.setHand(player1, List.of(new SettleTheScore()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Creature exiled
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);

        // Planeswalker gained 2 loyalty
        assertThat(planeswalker.getLoyaltyCounters()).isEqualTo(5);
    }

    @Test
    @DisplayName("Still exiles creature when no planeswalker is controlled")
    void exilesCreatureWithoutPlaneswalker() {
        Permanent creature = addReadyCreature(player2);

        harness.setHand(player1, List.of(new SettleTheScore()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Creature still exiled even with no planeswalker
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isNotEmpty();
    }

    @Test
    @DisplayName("Does not put loyalty counters on opponent's planeswalker")
    void doesNotAffectOpponentPlaneswalker() {
        Permanent creature = addReadyCreature(player2);
        Permanent oppPlaneswalker = addReadyPlaneswalker(player2, 3);

        harness.setHand(player1, List.of(new SettleTheScore()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Opponent's planeswalker should NOT gain loyalty counters
        assertThat(oppPlaneswalker.getLoyaltyCounters()).isEqualTo(3);
    }

    // ===== Helpers =====

    private Permanent addReadyCreature(Player player) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyPlaneswalker(Player player, int loyalty) {
        GarrukWildspeaker card = new GarrukWildspeaker();
        Permanent perm = new Permanent(card);
        perm.setLoyaltyCounters(loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
