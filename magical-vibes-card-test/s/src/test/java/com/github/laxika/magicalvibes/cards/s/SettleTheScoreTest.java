package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.j.JayaBallard;
import com.github.laxika.magicalvibes.cards.k.KarnScionOfUrza;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SettleTheScore.class, LlanowarElves.class, KarnScionOfUrza.class, JayaBallard.class})
class SettleTheScoreTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles target creature")
    void exilesTargetCreature() {
        Permanent creature = addCreatureReady(player2, new LlanowarElves());

        harness.setHand(player1, List.of(new SettleTheScore()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isNotEmpty();
    }

    @Test
    @DisplayName("Puts two loyalty counters on own planeswalker when exiling creature")
    void putsTwoLoyaltyCountersOnPlaneswalker() {
        Permanent creature = addCreatureReady(player2, new LlanowarElves());
        Permanent planeswalker = addReadyPlaneswalker(player1, 3);

        harness.setHand(player1, List.of(new SettleTheScore()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Creature exiled
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);

        // Planeswalker gained 2 loyalty
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("Still exiles creature when no planeswalker is controlled")
    void exilesCreatureWithoutPlaneswalker() {
        Permanent creature = addCreatureReady(player2, new LlanowarElves());

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
        Permanent creature = addCreatureReady(player2, new LlanowarElves());
        Permanent oppPlaneswalker = addReadyPlaneswalker(player2, 3);

        harness.setHand(player1, List.of(new SettleTheScore()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Opponent's planeswalker should NOT gain loyalty counters
        assertThat(oppPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Chooses one planeswalker during resolution after exiling the creature")
    void choosesOnePlaneswalkerDuringResolution() {
        Permanent creature = addCreatureReady(player2, new LlanowarElves());
        Permanent karn = addReadyPlaneswalker(player1, 3);
        Permanent jaya = harness.addToBattlefieldAndReturn(player1, new JayaBallard());
        jaya.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new SettleTheScore()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature.getCard());
        assertThat(karn.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(jaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);

        harness.handleMultiplePermanentsChosen(player1, List.of(jaya.getId()));

        assertThat(karn.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(jaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
    }

    @Test
    @DisplayName("Cannot decline mandatory loyalty counters when multiple planeswalkers are controlled")
    void cannotDeclineLoyaltyCounters() {
        Permanent creature = addCreatureReady(player2, new LlanowarElves());
        Permanent karn = addReadyPlaneswalker(player1, 3);
        Permanent jaya = harness.addToBattlefieldAndReturn(player1, new JayaBallard());
        jaya.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new SettleTheScore()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(karn.getId()));

        assertThat(karn.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(jaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not place loyalty counters when the creature target leaves before resolution")
    void illegalCreatureTargetPreventsLoyaltyCounters() {
        Permanent creature = addCreatureReady(player2, new LlanowarElves());
        Permanent planeswalker = addReadyPlaneswalker(player1, 3);
        harness.setHand(player1, List.of(new SettleTheScore()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castSorcery(player1, 0, creature.getId());

        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerGraveyards.get(player2.getId()).add(creature.getCard());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature.getCard());
    }

    private Permanent addReadyPlaneswalker(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new KarnScionOfUrza());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        return perm;
    }
}
