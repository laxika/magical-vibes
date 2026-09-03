package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(JetmirsFixer.class)
class JetmirsFixerTest extends BaseCardTest {

    @Test
    @DisplayName("Gives +1/+1 until end of turn without Treasure mana")
    void boostsWithoutTreasureMana() {
        Permanent fixer = addCreatureReady(player1, new JetmirsFixer());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(fixer.getPowerModifier()).isEqualTo(1);
        assertThat(fixer.getToughnessModifier()).isEqualTo(1);
        assertThat(fixer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when Treasure mana was spent")
    void putsCounterWithTreasureMana() {
        Permanent fixer = addCreatureReady(player1, new JetmirsFixer());
        addTreasureToken(player1);
        addTreasureToken(player1);

        activateTreasureAndChooseColor("RED");
        activateTreasureAndChooseColor("GREEN");

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(fixer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(fixer.getPowerModifier()).isZero();
        assertThat(fixer.getToughnessModifier()).isZero();
    }

    private void activateTreasureAndChooseColor(String color) {
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        int treasureIndex = -1;
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i).getCard().getName().equals("Treasure")) {
                treasureIndex = i;
                break;
            }
        }
        assertThat(treasureIndex).isGreaterThanOrEqualTo(0);

        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, color);
    }

    private void addTreasureToken(Player player) {
        Card treasureCard = new Card();
        treasureCard.setName("Treasure");
        treasureCard.setType(CardType.ARTIFACT);
        treasureCard.setManaCost("");
        treasureCard.setToken(true);
        treasureCard.setColor(null);
        treasureCard.setSubtypes(List.of(CardSubtype.TREASURE));
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
