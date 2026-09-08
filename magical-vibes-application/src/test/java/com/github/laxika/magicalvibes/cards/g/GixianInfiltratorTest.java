package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GixianInfiltratorTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another permanent puts a +1/+1 counter on Gixian Infiltrator")
    void sacrificingAnotherPermanentPutsCounterOnIt() {
        Permanent infiltrator = addCreatureReady(player1, new GixianInfiltrator());
        addTreasureToken(player1);

        int treasureIndex = findPermanentIndex(player1, "Treasure");
        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, "RED");
        resolveAllTriggers();

        assertThat(infiltrator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Treasure");
    }

    @Test
    @DisplayName("Each sacrificed permanent puts a separate counter on Gixian Infiltrator")
    void multipleSacrificesPutMultipleCountersOnIt() {
        Permanent infiltrator = addCreatureReady(player1, new GixianInfiltrator());
        addTreasureToken(player1);
        addTreasureToken(player1);

        sacrificeTreasure(player1);
        sacrificeTreasure(player1);

        assertThat(infiltrator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void sacrificeTreasure(Player player) {
        int treasureIndex = findPermanentIndex(player, "Treasure");
        harness.activateAbility(player, treasureIndex, null, null);
        harness.handleListChoice(player, "RED");
        resolveAllTriggers();
    }

    private int findPermanentIndex(Player player, String name) {
        List<Permanent> battlefield = gd.playerBattlefields.get(player.getId());
        for (int i = 0; i < battlefield.size(); i++) {
            if (name.equals(battlefield.get(i).getCard().getName())) {
                return i;
            }
        }
        throw new AssertionError("Permanent not found: " + name);
    }

    private void addTreasureToken(Player player) {
        Card treasureCard = new Card();
        treasureCard.setName("Treasure");
        treasureCard.setType(CardType.ARTIFACT);
        treasureCard.setToken(true);
        treasureCard.addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), new AwardAnyColorManaEffect()),
                "{T}, Sacrifice this artifact: Add one mana of any color."
        ));

        Permanent treasure = new Permanent(treasureCard);
        treasure.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(treasure);
    }
}
