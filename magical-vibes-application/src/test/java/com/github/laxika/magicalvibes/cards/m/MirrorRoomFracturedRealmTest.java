package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.ElvishVisionary;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.laxika.magicalvibes.model.ManaColor.BLUE;
import static com.github.laxika.magicalvibes.model.ManaColor.GREEN;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MirrorRoomFracturedRealm.class, ElvishVisionary.class})
class MirrorRoomFracturedRealmTest extends BaseCardTest {

    @Test
    void mirrorRoomCreatesAReflectionCopyOfATargetCreatureYouControl() {
        Card targetCard = creature("Target creature");
        Permanent target = addCreatureReady(player1, targetCard);

        castRoom(0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo("Target creature");
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.HUMAN, CardSubtype.REFLECTION);
    }

    @Test
    void fracturedRealmDoublesTriggeredAbilitiesOfPermanentsYouControl() {
        castRoom(1);

        harness.setHand(player1, List.of(new ElvishVisionary()));
        harness.addMana(player1, GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
    }

    private Permanent castRoom(int doorIndex) {
        harness.setHand(player1, List.of(new MirrorRoomFracturedRealm()));
        harness.addMana(player1, BLUE, doorIndex == 0 ? 3 : 7);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.ROOM))
                .findFirst()
                .orElseThrow();
    }

    private Card creature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(CardSubtype.HUMAN));
        card.setManaCost("{2}");
        card.setPower(2);
        card.setToughness(3);
        return card;
    }
}
