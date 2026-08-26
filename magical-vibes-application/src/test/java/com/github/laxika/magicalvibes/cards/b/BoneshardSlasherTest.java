package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CephalidIllusionist;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoneshardSlasher.class, CephalidIllusionist.class, Shock.class, Spellbook.class})
class BoneshardSlasherTest extends BaseCardTest {

    @Test
    @DisplayName("Remains a 1/1 with fewer than seven cards in its controller's graveyard")
    void noThresholdBoostBelowSevenCards() {
        harness.addToBattlefield(player1, new BoneshardSlasher());
        fillGraveyard(player1, 6);

        assertStats(1, 1);
    }

    @Test
    @DisplayName("Gets +2/+2 with seven cards in its controller's graveyard")
    void getsThresholdBoostAtSevenCards() {
        harness.addToBattlefield(player1, new BoneshardSlasher());
        fillGraveyard(player1, 7);

        assertStats(3, 3);
    }

    @Test
    @DisplayName("Loses the threshold boost when its controller's graveyard shrinks")
    void losesThresholdBoostWhenGraveyardShrinks() {
        harness.addToBattlefield(player1, new BoneshardSlasher());
        fillGraveyard(player1, 7);
        assertStats(3, 3);

        gd.playerGraveyards.get(player1.getId()).removeFirst();

        assertStats(1, 1);
    }

    @Test
    @DisplayName("Sacrifices itself when targeted by a spell")
    void sacrificesWhenTargetedBySpell() {
        Permanent slasher = harness.addToBattlefieldAndReturn(player1, new BoneshardSlasher());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, slasher.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Boneshard Slasher");
        harness.assertInGraveyard(player1, "Boneshard Slasher");
    }

    @Test
    @DisplayName("Sacrifices itself when targeted by an ability")
    void sacrificesWhenTargetedByAbility() {
        Permanent slasher = harness.addToBattlefieldAndReturn(player1, new BoneshardSlasher());
        Permanent illusionist = harness.addToBattlefieldAndReturn(player1, new CephalidIllusionist());
        illusionist.setSummoningSick(false);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int illusionistIndex = gd.playerBattlefields.get(player1.getId()).indexOf(illusionist);
        harness.activateAbility(player1, illusionistIndex, null, slasher.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Boneshard Slasher");
        harness.assertInGraveyard(player1, "Boneshard Slasher");
    }

    private void fillGraveyard(Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Spellbook());
        }
        harness.setGraveyard(player, cards);
    }

    private void assertStats(int power, int toughness) {
        Permanent slasher = findPermanent(player1, "Boneshard Slasher");
        assertThat(gqs.getEffectivePower(gd, slasher)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, slasher)).isEqualTo(toughness);
    }
}
