package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GrotagSiegeRunnerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself, destroys a creature with defender, and deals 2 damage to its controller")
    void destroysDefenderAndDealsDamageToController() {
        addReadyRunner(player1);
        Permanent wall = addDefender(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, wall.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grotag Siege-Runner");
        harness.assertInGraveyard(player2, "Test Wall");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals damage even when the targeted defender is saved from destruction")
    void dealsDamageWhenDestructionIsPrevented() {
        addReadyRunner(player1);
        Permanent wall = addDefender(player2);
        wall.setRegenerationShield(1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, wall.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Test Wall");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cannot target a creature without defender")
    void cannotTargetCreatureWithoutDefender() {
        addReadyRunner(player1);
        Permanent creature = addCreatureWithoutDefender(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with defender");
    }

    private Permanent addReadyRunner(Player player) {
        Permanent permanent = new Permanent(new GrotagSiegeRunner());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addDefender(Player player) {
        Card card = new Card();
        card.setName("Test Wall");
        card.setType(CardType.CREATURE);
        card.setPower(0);
        card.setToughness(4);
        card.setKeywords(Set.of(Keyword.DEFENDER));
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addCreatureWithoutDefender(Player player) {
        Card card = new Card();
        card.setName("Test Creature");
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
