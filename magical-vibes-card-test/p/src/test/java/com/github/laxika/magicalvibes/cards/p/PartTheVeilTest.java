package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KamiOfOldStone;
import com.github.laxika.magicalvibes.model.CardType;
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
        harness.assertNotInHand(player2, "Humble Budoka");
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
        HumbleBudoka ownedByOpponent = new HumbleBudoka();
        ownedByOpponent.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, ownedByOpponent);
        harness.addToBattlefield(player1, new KamiOfOldStone());
        harness.setHand(player2, java.util.List.of());

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
    @DisplayName("Does not return a creature owned by the caster but controlled by the opponent")
    void doesNotReturnOwnedCreatureControlledByOpponent() {
        HumbleBudoka ownedByCaster = new HumbleBudoka();
        ownedByCaster.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, ownedByCaster);

        harness.castFromHand(player1, new PartTheVeil(), "{3}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Humble Budoka");
        harness.assertNotInHand(player1, "Humble Budoka");
        harness.assertNotInHand(player2, "Humble Budoka");
        harness.assertInGraveyard(player1, "Part the Veil");
    }

    @Test
    @DisplayName("Returns creatures that entered after casting but before resolution")
    void checksCreaturesAtResolution() {
        harness.castFromHand(player1, new PartTheVeil(), "{3}{U}");
        harness.addToBattlefield(player1, new HumbleBudoka());
        harness.addToBattlefield(player2, new KamiOfOldStone());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Humble Budoka");
        harness.assertInHand(player1, "Humble Budoka");
        harness.assertOnBattlefield(player2, "Kami of Old Stone");
        harness.assertNotInHand(player2, "Kami of Old Stone");
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
