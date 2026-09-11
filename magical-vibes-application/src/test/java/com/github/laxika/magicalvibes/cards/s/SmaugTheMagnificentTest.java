package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
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
