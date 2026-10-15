package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GatherSpecimens;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BringerOfTheLastGift.class, BeaconOfUnrest.class, BitterTriumph.class, GatherSpecimens.class,
        GrizzlyBears.class, SavannahLions.class})
class BringerOfTheLastGiftTest extends BaseCardTest {

    @Test
    @DisplayName("When cast, sacrifices other creatures and returns pre-existing graveyard creatures")
    void castSacrificesOtherCreaturesAndReturnsPreExistingCreatures() {
        harness.setHand(player1, List.of(new BringerOfTheLastGift()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new SavannahLions());
        harness.setGraveyard(player1, List.of(new SavannahLions()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Bringer of the Last Gift")).isEqualTo(1);
        assertThat(countPermanents(player1, "Savannah Lions")).isEqualTo(1);
        assertThat(countPermanents(player2, "Grizzly Bears")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Savannah Lions");
    }

    @Test
    @DisplayName("When cast, does not return creatures sacrificed by its ability")
    void doesNotReturnCreaturesSacrificedByItsAbility() {
        harness.setHand(player1, List.of(new BringerOfTheLastGift()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new SavannahLions());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Savannah Lions");
        assertThat(countPermanents(player1, "Grizzly Bears")).isZero();
        assertThat(countPermanents(player2, "Savannah Lions")).isZero();
    }

    @Test
    @DisplayName("When put onto the battlefield without being cast, it does not trigger")
    void nonCastEtbDoesNotTrigger() {
        BringerOfTheLastGift target = new BringerOfTheLastGift();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Bringer of the Last Gift");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(countPermanents(player2, "Savannah Lions")).isZero();
    }

    @Test
    @DisplayName("Returns every pre-existing creature even when there are no other creatures to sacrifice")
    void returnsAllCreaturesWithoutSacrificesAndLeavesNoncreatureCards() {
        harness.setHand(player1, List.of(new BringerOfTheLastGift()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setGraveyard(player1, List.of(
                new BringerOfTheLastGift(), new BringerOfTheLastGift(), new BitterTriumph()));
        harness.setGraveyard(player2, List.of(new BringerOfTheLastGift(), new BitterTriumph()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Bringer of the Last Gift")).isEqualTo(3);
        assertThat(countPermanents(player2, "Bringer of the Last Gift")).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Bringer of the Last Gift");
        harness.assertNotInGraveyard(player2, "Bringer of the Last Gift");
        harness.assertInGraveyard(player1, "Bitter Triumph");
        harness.assertInGraveyard(player2, "Bitter Triumph");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Another Bringer is sacrificed while the ability's source survives")
    void sacrificesOtherCopiesOfBringer() {
        harness.setHand(player1, List.of(new BringerOfTheLastGift()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addToBattlefield(player1, new BringerOfTheLastGift());
        harness.addToBattlefield(player2, new BringerOfTheLastGift());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Bringer of the Last Gift")).isEqualTo(1);
        assertThat(countPermanents(player2, "Bringer of the Last Gift")).isZero();
        harness.assertInGraveyard(player1, "Bringer of the Last Gift");
        harness.assertInGraveyard(player2, "Bringer of the Last Gift");
    }

    @Test
    @DisplayName("A Bringer destroyed in response is returned by its own trigger without triggering again")
    void returnsSourceDestroyedBeforeTriggerResolves() {
        BringerOfTheLastGift bringer = new BringerOfTheLastGift();
        harness.setHand(player1, List.of(bringer, new BitterTriumph()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addToBattlefield(player2, new BringerOfTheLastGift());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstantWithDiscard(player1, 0,
                gd.playerBattlefields.get(player1.getId()).getFirst().getId(), null);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Bringer of the Last Gift");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bringer of the Last Gift");
        harness.assertNotInGraveyard(player1, "Bringer of the Last Gift");
        assertThat(countPermanents(player2, "Bringer of the Last Gift")).isZero();
        harness.assertInGraveyard(player2, "Bringer of the Last Gift");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when Gather Specimens gives it to a player who did not cast it")
    void enteringUnderNonCastersControlDoesNotTrigger() {
        harness.setHand(player1, List.of(new BringerOfTheLastGift()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setHand(player2, List.of(new GatherSpecimens()));
        harness.addMana(player2, ManaColor.BLUE, 6);
        harness.addToBattlefield(player1, new BringerOfTheLastGift());
        harness.addToBattlefield(player2, new BringerOfTheLastGift());
        harness.setGraveyard(player1, List.of(new BringerOfTheLastGift()));

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Bringer of the Last Gift")).isEqualTo(1);
        assertThat(countPermanents(player2, "Bringer of the Last Gift")).isEqualTo(2);
        harness.assertInGraveyard(player1, "Bringer of the Last Gift");
    }
}
