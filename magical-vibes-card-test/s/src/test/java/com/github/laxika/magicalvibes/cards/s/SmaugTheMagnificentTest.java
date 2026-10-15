package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SmaugTheMagnificent.class, GrizzlyBears.class})
class SmaugTheMagnificentTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Treasure token at the beginning of upkeep")
    void createsTreasureAtUpkeep() {
        addCreatureReady(player1, new SmaugTheMagnificent());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Deals damage equal to the number of Treasures controlled when it attacks")
    void dealsDamageEqualToTreasureCount() {
        addCreatureReady(player1, new SmaugTheMagnificent());
        addTreasure(player1);
        addTreasure(player1);
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not create a Treasure during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        addCreatureReady(player1, new SmaugTheMagnificent());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Can target a player and counts only its controller's Treasures")
    void damagesPlayerCountingOnlyControlledTreasures() {
        addCreatureReady(player1, new SmaugTheMagnificent());
        addTreasure(player1);
        addTreasure(player1);
        addTreasure(player2);
        harness.setLife(player2, 20);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, player2.getId());
            resolveAllTriggers();
        });

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Counts Treasures at resolution rather than when attacking")
    void countsTreasuresAtResolution() {
        addCreatureReady(player1, new SmaugTheMagnificent());
        Permanent treasure = addTreasure(player1);
        addTreasure(player1);
        harness.setLife(player2, 20);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, player2.getId());
            gd.playerBattlefields.get(player1.getId()).remove(treasure);
            resolveAllTriggers();
        });

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("An attack with no Treasures still targets but deals no damage")
    void dealsNoDamageWithoutTreasures() {
        addCreatureReady(player1, new SmaugTheMagnificent());
        addTreasure(player2);
        harness.setLife(player2, 20);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, player2.getId());
            resolveAllTriggers();
        });

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private Permanent addTreasure(Player player) {
        Card card = new Card();
        card.setName("Treasure");
        card.setType(CardType.ARTIFACT);
        card.setSubtypes(List.of(CardSubtype.TREASURE));
        card.setToken(true);

        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
