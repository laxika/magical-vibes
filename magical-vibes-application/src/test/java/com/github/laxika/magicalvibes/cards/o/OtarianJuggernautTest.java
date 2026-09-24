package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AngelicWall;
import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OtarianJuggernaut.class, AngelicWall.class, DuskImp.class, Forest.class})
class OtarianJuggernautTest extends BaseCardTest {

    @Test
    @DisplayName("Otarian Juggernaut gets +3/+0 at threshold")
    void getsThresholdBonus() {
        harness.setGraveyard(player1, graveyardCards(7));
        Permanent juggernaut = addCreatureReady(player1, new OtarianJuggernaut());

        assertThat(gqs.getEffectivePower(gd, juggernaut)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, juggernaut)).isEqualTo(3);
    }

    @Test
    @DisplayName("Otarian Juggernaut threshold counts only its controller's graveyard")
    void thresholdCountsOnlyControllersGraveyard() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, graveyardCards(7));
        Permanent juggernaut = addCreatureReady(player1, new OtarianJuggernaut());

        assertThat(gqs.getEffectivePower(gd, juggernaut)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, juggernaut)).isEqualTo(3);
    }

    @Test
    @DisplayName("Otarian Juggernaut has no threshold bonus below seven cards")
    void hasNoThresholdBonusBelowSevenCards() {
        harness.setGraveyard(player1, graveyardCards(6));
        Permanent juggernaut = addCreatureReady(player1, new OtarianJuggernaut());

        assertThat(gqs.getEffectivePower(gd, juggernaut)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, juggernaut)).isEqualTo(3);
    }

    @Test
    @DisplayName("Otarian Juggernaut must attack only while threshold is met")
    void mustAttackAtThresholdOnly() {
        Permanent juggernaut = addCreatureReady(player1, new OtarianJuggernaut());

        declareAttackers(List.of());
        assertThat(juggernaut.isAttacking()).isFalse();

        harness.setGraveyard(player1, graveyardCards(7));

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Otarian Juggernaut cannot be blocked by a Wall")
    void cannotBeBlockedByWall() {
        Permanent juggernaut = addCreatureReady(player1, new OtarianJuggernaut());
        juggernaut.setAttacking(true);

        Permanent wall = addCreatureReady(player2, new AngelicWall());

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(wall);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(juggernaut);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    @Test
    @DisplayName("Otarian Juggernaut can be blocked by a non-Wall creature")
    void canBeBlockedByNonWall() {
        Permanent juggernaut = addCreatureReady(player1, new OtarianJuggernaut());
        juggernaut.setAttacking(true);

        Permanent nonWall = addCreatureReady(player2, new DuskImp());

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(nonWall);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(juggernaut);
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(nonWall.isBlocking()).isTrue();
    }

    private List<Card> graveyardCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Forest());
        }
        return cards;
    }
}
