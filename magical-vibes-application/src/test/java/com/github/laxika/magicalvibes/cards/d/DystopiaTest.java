package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DystopiaTest extends BaseCardTest {

    @Test
    @DisplayName("At a player's upkeep that player sacrifices a green permanent")
    void greenPermanentSacrificedAtThatPlayersUpkeep() {
        harness.addToBattlefield(player1, new Dystopia());
        Permanent green = addCreature(player2, "Green Bear", CardColor.GREEN);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(green.getId()));
    }

    @Test
    @DisplayName("A black permanent is not sacrificed")
    void blackPermanentNotSacrificed() {
        harness.addToBattlefield(player1, new Dystopia());
        Permanent black = addCreature(player2, "Black Zombie", CardColor.BLACK);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(black.getId()));
    }

    @Test
    @DisplayName("A land is colorless and is not sacrificed")
    void landNotSacrificed() {
        harness.addToBattlefield(player1, new Dystopia());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(forest.getId()));
    }

    @Test
    @DisplayName("With several green or white permanents the player chooses which to sacrifice")
    void choosesAmongMatchingPermanents() {
        harness.addToBattlefield(player1, new Dystopia());
        Permanent green = addCreature(player2, "Green Bear", CardColor.GREEN);
        Permanent white = addCreature(player2, "White Knight", CardColor.WHITE);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultiplePermanentsChosen(player2, List.of(white.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(white.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(green.getId()));
    }

    @Test
    @DisplayName("Paying the cumulative upkeep costs 1 life per age counter")
    void paysCumulativeUpkeepInLife() {
        Permanent dystopia = harness.addToBattlefieldAndReturn(player1, new Dystopia());
        dystopia.setCounterCount(CounterType.AGE, 1);
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        // Both of Dystopia's upkeep triggers go on the stack at its controller's upkeep; the
        // sacrifice trigger is a no-op here (no green or white permanents), the cumulative
        // upkeep trigger prompts.
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(dystopia.getCounterCount(CounterType.AGE)).isEqualTo(2);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dystopia);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Declining the cumulative upkeep sacrifices Dystopia")
    void decliningUpkeepSacrifices() {
        Permanent dystopia = harness.addToBattlefieldAndReturn(player1, new Dystopia());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(dystopia);
        harness.assertInGraveyard(player1, "Dystopia");
    }

    private Permanent addCreature(Player player, String name, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        card.setColor(color);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
