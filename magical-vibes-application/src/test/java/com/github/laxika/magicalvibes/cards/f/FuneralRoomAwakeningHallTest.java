package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FuneralRoomAwakeningHall.class, GrizzlyBears.class, WrathOfGod.class})
class FuneralRoomAwakeningHallTest extends BaseCardTest {

    @Test
    void funeralRoomMakesEachOpponentLoseLifeAndYouGainLifeWhenYourCreatureDies() {
        castRoom(0);
        harness.addToBattlefield(player1, new GrizzlyBears());
        int player1LifeBefore = gd.getLife(player1.getId());
        int player2LifeBefore = gd.getLife(player2.getId());

        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1LifeBefore + 1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore - 1);
    }

    @Test
    void unlockingAwakeningHallReturnsAllCreatureCardsFromYourGraveyard() {
        GrizzlyBears firstCreature = new GrizzlyBears();
        GrizzlyBears secondCreature = new GrizzlyBears();
        Card noncreature = new Card();
        noncreature.setName("Noncreature card");
        noncreature.setType(CardType.ENCHANTMENT);
        harness.setGraveyard(player1, List.of(firstCreature, secondCreature, noncreature));

        Permanent room = castRoom(0);
        harness.addMana(player1, ManaColor.BLACK, 8);
        harness.unlockRoomDoor(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(room), 1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(noncreature);
    }

    private Permanent castRoom(int doorIndex) {
        harness.setHand(player1, List.of(new FuneralRoomAwakeningHall()));
        harness.addMana(player1, ManaColor.BLACK, doorIndex == 0 ? 3 : 8);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }
}
