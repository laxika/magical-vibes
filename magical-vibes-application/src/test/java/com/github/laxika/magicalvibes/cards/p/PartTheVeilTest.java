package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KamiOfOldStone;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PartTheVeil.class, HumbleBudoka.class, KamiOfOldStone.class, Island.class})
class PartTheVeilTest extends BaseCardTest {

    @Test
    @DisplayName("Returns only the caster's creatures to hand")
    void returnsOnlyControllersCreatures() {
        harness.addToBattlefield(player1, new HumbleBudoka());
        harness.addToBattlefield(player1, new KamiOfOldStone());
        harness.addToBattlefield(player2, new HumbleBudoka());

        harness.castFromHand(player1, new PartTheVeil(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.CREATURE));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.CREATURE));

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactlyInAnyOrder("Humble Budoka", "Kami of Old Stone");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(c -> c.getName())
                .doesNotContain("Humble Budoka");
    }

    @Test
    @DisplayName("Does not return the caster's non-creature permanents")
    void doesNotReturnNonCreatures() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new HumbleBudoka());

        harness.castFromHand(player1, new PartTheVeil(), "{3}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Island");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Humble Budoka");
    }

    @Test
    @DisplayName("Returns a creature to its owner's hand even when another player controls it")
    void returnsControlledCreatureToItsOwnersHand() {
        Permanent stolenCreature = harness.addToBattlefieldAndReturn(player1, new HumbleBudoka());
        gd.stolenCreatures.put(stolenCreature.getId(), player2.getId());
        harness.addToBattlefield(player1, new KamiOfOldStone());

        harness.castFromHand(player1, new PartTheVeil(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Kami of Old Stone");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Humble Budoka");
    }

    @Test
    @DisplayName("Resolves with no creatures on the battlefield and goes to the graveyard")
    void resolvesWithEmptyBattlefield() {
        harness.castFromHand(player1, new PartTheVeil(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Part the Veil");
    }
}
