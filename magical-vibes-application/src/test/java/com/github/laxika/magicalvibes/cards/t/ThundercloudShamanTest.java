package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThundercloudShamanTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals damage equal to the number of Giants you control (including itself) to each non-Giant creature")
    void etbDealsDamageEqualToGiantCount() {
        // player1 already controls another Giant; the Shaman itself makes two Giants total.
        harness.addToBattlefield(player1, makeCreature("Giant Ally", 4, 4, CardSubtype.GIANT));
        Permanent target = harness.addToBattlefieldAndReturn(player2, makeCreature("Grizzly Bears", 2, 3));

        castShaman(player1);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB does not damage Giant creatures")
    void etbDoesNotDamageGiants() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, makeCreature("Hill Giant", 3, 3, CardSubtype.GIANT));

        castShaman(player1);

        assertThat(giant.getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("ETB destroys non-Giant creatures with toughness at or below the damage")
    void etbKillsSmallNonGiants() {
        // Only the Shaman itself is a Giant -> 1 damage.
        harness.addToBattlefield(player2, makeCreature("Goblin", 1, 1));

        castShaman(player1);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Goblin"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Goblin"));
    }

    // ===== Helpers =====

    private void castShaman(Player player) {
        harness.setHand(player, List.of(new ThundercloudShaman()));
        harness.addMana(player, ManaColor.RED, 5);
        harness.castCreature(player, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger
    }

    private Card makeCreature(String name, int power, int toughness, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        card.setSubtypes(List.of(subtypes));
        return card;
    }
}
