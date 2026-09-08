package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RestrictedOfficeLectureHall.class, Forest.class, GrizzlyBears.class})
class RestrictedOfficeLectureHallTest extends BaseCardTest {

    @Test
    void restrictedOfficeDestroysCreaturesWithPowerAtLeastThree() {
        addCreatureReady(player1, creature("Large own creature", 3, 3));
        addCreatureReady(player1, creature("Small own creature", 2, 2));
        addCreatureReady(player2, creature("Large opposing creature", 4, 4));

        castRoom(0);

        harness.assertNotOnBattlefield(player1, "Large own creature");
        harness.assertOnBattlefield(player1, "Small own creature");
        harness.assertNotOnBattlefield(player2, "Large opposing creature");
    }

    @Test
    void lectureHallGrantsHexproofToOtherPermanentsYouControlAfterUnlockingTheDoor() {
        Permanent room = castRoom(0);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownLand, Keyword.HEXPROOF)).isFalse();

        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.unlockRoomDoor(player1, gd.playerBattlefields.get(player1.getId()).indexOf(room), 1);

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownLand, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, room, Keyword.HEXPROOF)).isFalse();
    }

    private Permanent castRoom(int doorIndex) {
        harness.setHand(player1, List.of(new RestrictedOfficeLectureHall()));
        harness.addMana(player1, doorIndex == 0 ? ManaColor.WHITE : ManaColor.BLUE,
                doorIndex == 0 ? 4 : 7);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    private Card creature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
