package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VodalianSerpent.class, Island.class, Plains.class})
class VodalianSerpentTest extends BaseCardTest {

    @Test
    @DisplayName("Vodalian Serpent enters without counters when not kicked")
    void entersWithoutCountersWhenNotKicked() {
        harness.castFromHand(player1, new VodalianSerpent(), "{3}{U}");
        harness.passBothPriorities();

        Permanent serpent = findPermanent(player1, "Vodalian Serpent");
        assertThat(serpent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Vodalian Serpent enters with four +1/+1 counters when kicked")
    void entersWithCountersWhenKicked() {
        harness.setHand(player1, List.of(new VodalianSerpent()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent serpent = findPermanent(player1, "Vodalian Serpent");
        assertThat(serpent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Vodalian Serpent can attack when defending player controls an Island")
    void canAttackWhenDefenderControlsIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new Island());

        addCreatureReady(player1, new VodalianSerpent());
        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Vodalian Serpent cannot attack when defending player controls no Island")
    void cannotAttackWhenDefenderControlsNoIsland() {
        addCreatureReady(player1, new VodalianSerpent());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Vodalian Serpent cannot attack when only the attacking player controls an Island")
    void cannotAttackWhenOnlyAttackingPlayerControlsIsland() {
        addCreatureReady(player1, new VodalianSerpent());
        harness.addToBattlefield(player1, new Island());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Vodalian Serpent cannot attack when defending player controls a non-Island land")
    void cannotAttackWhenDefenderControlsNonIslandLand() {
        addCreatureReady(player1, new VodalianSerpent());
        harness.addToBattlefield(player2, new Plains());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }
}
