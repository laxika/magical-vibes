package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class WindstormTest extends BaseCardTest {

    /** A 2/2 flying creature for test purposes. */
    private static Card flyingCreature() {
        Card card = new Card();
        card.setName("Wind Drake");
        card.setType(CardType.CREATURE);
        card.setManaCost("{2}{U}");
        card.setColor(CardColor.BLUE);
        card.setPower(2);
        card.setToughness(2);
        card.setKeywords(Set.of(Keyword.FLYING));
        return card;
    }

    @Test
    @DisplayName("Casting Windstorm puts it on the stack as an instant spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new Windstorm()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0, 3, null);

        GameData gd = harness.getGameData();

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Windstorm");
        assertThat(entry.getXValue()).isEqualTo(3);
    }

    @Test
    @DisplayName("Deals X damage to creatures with flying")
    void dealsXDamageToFlyingCreatures() {
        harness.addToBattlefield(player1, flyingCreature());
        harness.addToBattlefield(player2, flyingCreature());

        harness.setHand(player1, List.of(new Windstorm()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0, 2, null);

        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Both flying creatures should be destroyed (2 damage >= 2 toughness)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Wind Drake"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Wind Drake"));
    }

    @Test
    @DisplayName("Does not damage non-flying creatures")
    void doesNotDamageNonFlyingCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Windstorm()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castInstant(player1, 0, 3, null);

        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Non-flying creature survives
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Does not damage players")
    void doesNotDamagePlayers() {
        harness.setHand(player1, List.of(new Windstorm()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castInstant(player1, 0, 3, null);

        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Players stay at 20 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("X=0 deals no damage")
    void xZeroDealsNoDamage() {
        harness.addToBattlefield(player2, flyingCreature());

        harness.setHand(player1, List.of(new Windstorm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, 0, null);

        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Flying creature survives with 0 damage
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Wind Drake"));
    }
}
