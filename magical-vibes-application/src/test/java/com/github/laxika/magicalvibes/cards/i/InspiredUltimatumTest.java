package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InspiredUltimatum.class, GrizzlyBears.class, HillGiant.class})
class InspiredUltimatumTest extends BaseCardTest {

    @Test
    void targetPlayerGainsLifeAnyTargetTakesDamageAndControllerDrawsFive() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        List<Card> drawnCards = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears());
        harness.setLife(player2, 10);
        harness.setLibrary(player1, drawnCards);
        harness.setHand(player1, List.of(new InspiredUltimatum()));
        addMana();

        harness.castSorcery(player1, 0, List.of(player2.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(creature.getId()));
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrderElementsOf(drawnCards.stream().map(Card::getId).toList());
    }

    @Test
    void samePlayerMayBeChosenForLifeGainAndDamage() {
        List<Card> drawnCards = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears());
        harness.setLife(player2, 10);
        harness.setLibrary(player1, drawnCards);
        harness.setHand(player1, List.of(new InspiredUltimatum()));
        addMana();

        harness.castSorcery(player1, 0, List.of(player2.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrderElementsOf(drawnCards.stream().map(Card::getId).toList());
    }

    @Test
    void lifeGainTargetMustBeAPlayer() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new InspiredUltimatum()));
        addMana();

        assertThatThrownBy(() ->
                harness.castSorcery(player1, 0, List.of(creature.getId(), player2.getId()))
        ).isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);
    }
}
