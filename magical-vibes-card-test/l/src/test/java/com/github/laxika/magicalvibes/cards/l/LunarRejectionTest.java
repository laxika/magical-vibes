package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.ChildOfThePack;
import com.github.laxika.magicalvibes.cards.h.HungryRidgewolf;
import com.github.laxika.magicalvibes.cards.r.RepositorySkaab;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LunarRejection.class, Forest.class,
        HungryRidgewolf.class, RepositorySkaab.class, ChildOfThePack.class})
class LunarRejectionTest extends BaseCardTest {

    @Test
    void normalCastReturnsWolfOrWerewolfAndDrawsACard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2,
                new HungryRidgewolf());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new LunarRejection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Hungry Ridgewolf");
        harness.assertInHand(player2, "Hungry Ridgewolf");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void normalCastCannotTargetOtherCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2,
                new RepositorySkaab());
        harness.setHand(player1, List.of(new LunarRejection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Wolf or Werewolf");
    }

    @Test
    void cleaveCastReturnsAnyCreatureAndDrawsACard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2,
                new RepositorySkaab());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new LunarRejection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Repository Skaab");
        harness.assertInHand(player2, "Repository Skaab");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void cleaveCastCannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new LunarRejection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void normalCastReturnsWerewolfAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2,
                new ChildOfThePack());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new LunarRejection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Child of the Pack");
        harness.assertInHand(player2, "Child of the Pack");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void doesNotDrawWhenTheOnlyTargetLeavesTheBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HungryRidgewolf());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new LunarRejection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Lunar Rejection");
    }

    @Test
    void returnsCreatureToOwnerInsteadOfItsController() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HungryRidgewolf());
        gd.stolenCreatures.put(target.getId(), player1.getId());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new LunarRejection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Hungry Ridgewolf");
        harness.assertInHand(player1, "Hungry Ridgewolf");
        harness.assertNotInHand(player2, "Hungry Ridgewolf");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void cleaveReturnsRealNonWolfCreatureAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RepositorySkaab());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new LunarRejection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Repository Skaab");
        harness.assertInHand(player2, "Repository Skaab");
        harness.assertInHand(player1, "Forest");
    }

}
