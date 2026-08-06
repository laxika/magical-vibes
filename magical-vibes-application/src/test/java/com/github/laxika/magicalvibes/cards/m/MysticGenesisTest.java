package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MysticGenesisTest extends BaseCardTest {

    private void prepare() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new MysticGenesis()));
        harness.addMana(player2, ManaColor.BLUE, 4); // {2}{G}{U}{U}
        harness.addMana(player2, ManaColor.GREEN, 1);
    }

    private List<Permanent> tokensOf(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();
    }

    @Test
    @DisplayName("Counters the spell and creates an Ooze token sized to that spell's mana value")
    void countersAndCreatesOozeSizedToManaValue() {
        prepare();
        GrizzlyBears bears = new GrizzlyBears(); // mana value 2
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");

        List<Permanent> tokens = tokensOf(player2);
        assertThat(tokens).hasSize(1);
        Permanent ooze = tokens.getFirst();
        assertThat(ooze.getCard().getName()).isEqualTo("Ooze");
        assertThat(ooze.getCard().getPower()).isEqualTo(2);
        assertThat(ooze.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Token size follows the countered spell's mana value")
    void tokenSizeFollowsCounteredSpell() {
        prepare();
        SerraAngel angel = new SerraAngel(); // mana value 5
        harness.setHand(player1, List.of(angel));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, angel.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Serra Angel");

        List<Permanent> tokens = tokensOf(player2);
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().getPower()).isEqualTo(5);
        assertThat(tokens.getFirst().getCard().getToughness()).isEqualTo(5);
    }
}
