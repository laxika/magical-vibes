package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BattleScreech;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Envelop.class, BattleScreech.class, SuntailHawk.class})
class EnvelopTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack targeting a sorcery spell")
    void castingTargetsSorcerySpell() {
        BattleScreech battleScreech = new BattleScreech();
        harness.setHand(player1, List.of(battleScreech));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.setHand(player2, List.of(new Envelop()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, battleScreech.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry envelopEntry = gd.stack.getLast();
        assertThat(envelopEntry.getTargetId()).isEqualTo(battleScreech.getId());
    }

    @Test
    @DisplayName("Resolving counters the sorcery spell")
    void countersSorcerySpell() {
        BattleScreech battleScreech = new BattleScreech();
        harness.setHand(player1, List.of(battleScreech));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.setHand(player2, List.of(new Envelop()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, battleScreech.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Battle Screech");
        harness.assertNotOnBattlefield(player1, "Bird");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a non-sorcery spell")
    void cannotTargetNonSorcerySpell() {
        SuntailHawk hawk = new SuntailHawk();
        harness.setHand(player1, List.of(hawk));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.setHand(player2, List.of(new Envelop()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, hawk.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
