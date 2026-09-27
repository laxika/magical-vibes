package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MyWealthWillBuryYou.class, DarksteelIngot.class, AngelicChorus.class})
class MyWealthWillBuryYouTest extends BaseCardTest {

    @Test
    void createsAtLeastFourTreasuresFromOpponentArtifactsAndEnchantments() {
        addOpponentArtifactsAndEnchantments(2, 1);
        harness.addToBattlefield(player1, new DarksteelIngot());
        harness.addToBattlefield(player1, new AngelicChorus());

        resolveScheme();

        assertThat(findPermanents(player1, "Treasure")).hasSize(4);
    }

    @Test
    void createsOneTreasurePerOpponentArtifactOrEnchantmentAboveTheMinimum() {
        addOpponentArtifactsAndEnchantments(3, 2);
        harness.addToBattlefield(player1, new DarksteelIngot());
        harness.addToBattlefield(player1, new AngelicChorus());

        resolveScheme();

        assertThat(findPermanents(player1, "Treasure")).hasSize(5);
    }

    private void addOpponentArtifactsAndEnchantments(int artifacts, int enchantments) {
        for (int i = 0; i < artifacts; i++) {
            harness.addToBattlefield(player2, new DarksteelIngot());
        }
        for (int i = 0; i < enchantments; i++) {
            harness.addToBattlefield(player2, new AngelicChorus());
        }
    }

    private void resolveScheme() {
        Card scheme = new MyWealthWillBuryYou();
        gd.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getEffects(EffectSlot.SPELL)));
        harness.passBothPriorities();
    }
}
