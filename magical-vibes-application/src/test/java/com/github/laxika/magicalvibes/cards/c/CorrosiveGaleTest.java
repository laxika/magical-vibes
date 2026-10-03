package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AlloyMyr;
import com.github.laxika.magicalvibes.cards.h.Hovermyr;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CorrosiveGale.class, Hovermyr.class, AlloyMyr.class})
class CorrosiveGaleTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Corrosive Gale puts it on the stack with correct X value")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new CorrosiveGale()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 3);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getXValue()).isEqualTo(3);
    }

    @Test
    @DisplayName("Deals X damage to creatures with flying")
    void dealsXDamageToFlyingCreatures() {
        harness.addToBattlefield(player1, new Hovermyr());
        harness.addToBattlefield(player2, new Hovermyr());

        harness.setHand(player1, List.of(new CorrosiveGale()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveSorcery(player1, 0, 2);

        // Both flying creatures should be destroyed (2 damage >= 2 toughness)
        harness.assertNotOnBattlefield(player1, "Hovermyr");
        harness.assertNotOnBattlefield(player2, "Hovermyr");
    }

    @Test
    @DisplayName("Does not damage non-flying creatures")
    void doesNotDamageNonFlyingCreatures() {
        harness.addToBattlefield(player2, new AlloyMyr());

        harness.setHand(player1, List.of(new CorrosiveGale()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveSorcery(player1, 0, 3);

        // Non-flying creature survives
        harness.assertOnBattlefield(player2, "Alloy Myr");
    }

    @Test
    @DisplayName("Does not damage players (unlike Hurricane)")
    void doesNotDamagePlayers() {
        harness.setHand(player1, List.of(new CorrosiveGale()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveSorcery(player1, 0, 3);

        // Players stay at 20 life
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can be cast by paying Phyrexian mana with 2 life instead of green")
    void canPayPhyrexianManaWithLife() {
        harness.addToBattlefield(player2, new Hovermyr());

        harness.setHand(player1, List.of(new CorrosiveGale()));
        // Red mana pays X; the green Phyrexian symbol is paid with 2 life.
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, 2);

        // Player paid 2 life for Phyrexian mana
        harness.assertLife(player1, 18);

        // Flying creature should be destroyed
        harness.assertNotOnBattlefield(player2, "Hovermyr");
    }

    @Test
    @DisplayName("X=0 deals no damage")
    void xZeroDealsNoDamage() {
        harness.addToBattlefield(player2, new Hovermyr());

        harness.setHand(player1, List.of(new CorrosiveGale()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveSorcery(player1, 0, 0);

        // Flying creature survives with 0 damage
        harness.assertOnBattlefield(player2, "Hovermyr");
    }
    @Test
    @DisplayName("Marks exactly X damage when a flying creature survives")
    void marksSublethalDamage() {
        harness.addToBattlefield(player2, new Hovermyr());
        harness.setHand(player1, List.of(new CorrosiveGale()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 1);

        harness.assertOnBattlefield(player2, "Hovermyr");
        assertThat(findPermanent(player2, "Hovermyr").getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Uses flying at resolution rather than at casting")
    void checksFlyingAtResolution() {
        harness.addToBattlefield(player1, new AlloyMyr());
        harness.addToBattlefield(player2, new Hovermyr());
        Permanent newlyFlying = findPermanent(player1, "Alloy Myr");
        Permanent noLongerFlying = findPermanent(player2, "Hovermyr");
        harness.setHand(player1, List.of(new CorrosiveGale()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castSorcery(player1, 0, 2);

        newlyFlying.getGrantedKeywords().add(Keyword.FLYING);
        noLongerFlying.getRemovedKeywords().add(Keyword.FLYING);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Alloy Myr");
        harness.assertOnBattlefield(player2, "Hovermyr");
        assertThat(noLongerFlying.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("X zero can be paid with life and no mana")
    void xZeroCanBePaidWithLife() {
        harness.addToBattlefield(player2, new Hovermyr());
        harness.setHand(player1, List.of(new CorrosiveGale()));

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Hovermyr");
        assertThat(findPermanent(player2, "Hovermyr").getMarkedDamage()).isZero();
    }
}
