package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(GlassworksShatteredYard.class)
class GlassworksShatteredYardTest extends BaseCardTest {

    @Test
    void glassworksDealsFourDamageToATargetCreatureAnOpponentControls() {
        Permanent target = addCreatureReady(player2, creature("Target creature", 5, 5));

        castRoom(0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void glassworksCannotTargetACreatureItsControllerControls() {
        Permanent ownCreature = addCreatureReady(player1, creature("Own creature", 5, 5));
        addCreatureReady(player2, creature("Opponent creature", 5, 5));

        castRoom(0);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    void shatteredYardDealsOneDamageToEachOpponentAtYourEndStep() {
        int player1Life = gd.getLife(player1.getId());
        int player2Life = gd.getLife(player2.getId());
        castRoom(1);

        forceEndStep(player1);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1Life);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2Life - 1);
    }

    private Permanent castRoom(int doorIndex) {
        harness.setHand(player1, List.of(new GlassworksShatteredYard()));
        harness.addMana(player1, ManaColor.RED, doorIndex == 0 ? 3 : 5);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        harness.passBothPriorities();
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

    private Card creature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.RED);
        card.setManaCost("{1}{R}");
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
