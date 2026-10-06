package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CautiousSurvivor;
import com.github.laxika.magicalvibes.cards.e.Exorcise;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.InnocuousRat;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RestrictedOfficeLectureHall.class, Forest.class, InnocuousRat.class, CautiousSurvivor.class, Exorcise.class})
class RestrictedOfficeLectureHallTest extends BaseCardTest {

    @Test
    void restrictedOfficeDestroysCreaturesWithPowerAtLeastThree() {
        Permanent largeOwnCreature = addCreatureReady(player1, new CautiousSurvivor());
        largeOwnCreature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Permanent smallOwnCreature = addCreatureReady(player1, new InnocuousRat());
        smallOwnCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player2, new CautiousSurvivor());

        castRoom(0);

        harness.assertNotOnBattlefield(player1, "Cautious Survivor");
        harness.assertOnBattlefield(player1, "Innocuous Rat");
        harness.assertNotOnBattlefield(player2, "Cautious Survivor");
    }

    @Test
    void lectureHallGrantsHexproofToOtherPermanentsYouControlAfterUnlockingTheDoor() {
        Permanent room = castRoom(0);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new InnocuousRat());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new InnocuousRat());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownLand, Keyword.HEXPROOF)).isFalse();

        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.unlockRoomDoor(player1, gd.playerBattlefields.get(player1.getId()).indexOf(room), 1);

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownLand, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, room, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void castingLectureHallProtectsExistingAndLaterPermanentsWithoutDestroyingCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new CautiousSurvivor());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new CautiousSurvivor());

        Permanent room = castRoom(1);
        Permanent laterLand = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.assertOnBattlefield(player1, "Cautious Survivor");
        harness.assertOnBattlefield(player2, "Cautious Survivor");
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, laterLand, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, room, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void unlockingRestrictedOfficeAfterLectureHallDestroysEvenHexproofCreatures() {
        Permanent room = castRoom(1);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new CautiousSurvivor());
        harness.addToBattlefield(player2, new CautiousSurvivor());
        harness.addToBattlefield(player1, new InnocuousRat());
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HEXPROOF)).isTrue();
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.unlockRoomDoor(player1, gd.playerBattlefields.get(player1.getId()).indexOf(room), 0);
        harness.assertOnBattlefield(player1, "Cautious Survivor");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Cautious Survivor");
        harness.assertNotOnBattlefield(player2, "Cautious Survivor");
        harness.assertInGraveyard(player1, "Cautious Survivor");
        harness.assertInGraveyard(player2, "Cautious Survivor");
        harness.assertOnBattlefield(player1, "Innocuous Rat");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(room);
    }

    @Test
    void lectureHallStopsGrantingHexproofWhenItLeavesTheBattlefield() {
        Permanent room = castRoom(1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new InnocuousRat());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HEXPROOF)).isTrue();

        harness.setHand(player1, List.of(new Exorcise()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castSorcery(player1, 0, room.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(room);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void restrictedOfficeChecksCurrentPowerWhenItsTriggerResolves() {
        Permanent room = castRoom(1);
        Permanent grows = harness.addToBattlefieldAndReturn(player1, new CautiousSurvivor());
        grows.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        Permanent shrinks = harness.addToBattlefieldAndReturn(player2, new CautiousSurvivor());

        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.unlockRoomDoor(player1, gd.playerBattlefields.get(player1.getId()).indexOf(room), 0);
        grows.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        shrinks.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(grows);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(shrinks);
        harness.assertInGraveyard(player1, "Cautious Survivor");
    }

    @Test
    void restrictedOfficeDoesNotDestroyIndestructibleCreatures() {
        Permanent indestructible = harness.addToBattlefieldAndReturn(player2, new CautiousSurvivor());
        indestructible.setCounterCount(CounterType.INDESTRUCTIBLE, 1);

        castRoom(0);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(indestructible);
        harness.assertNotInGraveyard(player2, "Cautious Survivor");
    }

    private Permanent castRoom(int doorIndex) {
        harness.setHand(player1, List.of(new RestrictedOfficeLectureHall()));
        harness.addMana(player1, doorIndex == 0 ? ManaColor.WHITE : ManaColor.BLUE,
                doorIndex == 0 ? 4 : 7);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        resolveAllTriggers();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof RestrictedOfficeLectureHall)
                .findFirst().orElseThrow();
    }

}
