package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AdiposeOffspring.class, GrizzlyBears.class})
class AdiposeOffspringTest extends BaseCardTest {

    @Test
    void hardcastCreatesOneAlien() {
        harness.setHand(player1, List.of(new AdiposeOffspring()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Alien")).hasSize(1);
    }

    @Test
    void emergeCreatesAliensEqualToSacrificedToughness() {
        var sacrificedId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new AdiposeOffspring()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(sacrificedId));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Alien")).hasSize(2);
    }

    @Test
    void enteringWithoutBeingCastCreatesOneAlienForItsController() {
        harness.enterBattlefieldAndReturn(player2, new AdiposeOffspring());
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Alien")).hasSize(1);
        assertThat(findPermanents(player1, "Alien")).isEmpty();
    }

    @Test
    void emergeUsesModifiedToughnessRatherThanRemainingUndamagedToughness() {
        var sacrificed = harness.addToBattlefieldAndReturn(player1, new AdiposeOffspring());
        sacrificed.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        sacrificed.setMarkedDamage(3);
        harness.setHand(player1, List.of(new AdiposeOffspring()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(sacrificed.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrificed);
        harness.assertInGraveyard(player1, "Adipose Offspring");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Alien")).hasSize(4);
    }

    @Test
    void emergeCanSacrificeATokenWithoutReducingManaCost() {
        harness.setHand(player1, List.of(new AdiposeOffspring()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        var tokenId = harness.getPermanentId(player1, "Alien");
        harness.setHand(player1, List.of(new AdiposeOffspring()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(tokenId));
        assertThat(findPermanents(player1, "Alien")).isEmpty();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Alien")).hasSize(2);
    }
}
