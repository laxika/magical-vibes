package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(UnholyAnnexRitualChamber.class)
class UnholyAnnexRitualChamberTest extends BaseCardTest {

    @Test
    void unholyAnnexDrawsAndYouLoseLifeWithoutADemon() {
        Card draw = new Card();
        draw.setName("Draw");
        harness.setLibrary(player1, List.of(draw));
        castRoom(0);

        int player1LifeBefore = gd.getLife(player1.getId());
        int player2LifeBefore = gd.getLife(player2.getId());
        forceEndStep(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
        assertThat(gd.getLife(player1.getId())).isEqualTo(player1LifeBefore - 2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore);
    }

    @Test
    void unholyAnnexDrawsDrainsOpponentsAndGainsLifeWithADemon() {
        Card demon = new Card();
        demon.setName("Demon");
        demon.setType(CardType.CREATURE);
        demon.setColor(CardColor.BLACK);
        demon.setSubtypes(List.of(CardSubtype.DEMON));
        demon.setPower(2);
        demon.setToughness(2);
        harness.addToBattlefield(player1, demon);

        Card draw = new Card();
        draw.setName("Draw");
        harness.setLibrary(player1, List.of(draw));
        castRoom(0);

        int player1LifeBefore = gd.getLife(player1.getId());
        int player2LifeBefore = gd.getLife(player2.getId());
        forceEndStep(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
        assertThat(gd.getLife(player1.getId())).isEqualTo(player1LifeBefore + 2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore - 2);
    }

    @Test
    void ritualChamberCreatesADemonTokenWhenUnlocked() {
        castRoom(1);

        Permanent demon = findPermanent(player1, "Demon");
        assertThat(demon.getEffectivePower()).isEqualTo(6);
        assertThat(demon.getEffectiveToughness()).isEqualTo(6);
        assertThat(demon.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(demon.getCard().getSubtypes()).containsExactly(CardSubtype.DEMON);
        assertThat(demon.hasKeyword(Keyword.FLYING)).isTrue();
    }

    private Permanent castRoom(int doorIndex) {
        harness.setHand(player1, List.of(new UnholyAnnexRitualChamber()));
        harness.addMana(player1, ManaColor.BLACK, doorIndex == 0 ? 3 : 5);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.ROOM))
                .findFirst().orElseThrow();
    }

    private void forceEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
