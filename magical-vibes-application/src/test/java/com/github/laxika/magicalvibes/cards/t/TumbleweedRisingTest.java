package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TumbleweedRising.class, GrizzlyBears.class, HillGiant.class})
class TumbleweedRisingTest extends BaseCardTest {

    @Test
    @DisplayName("Creates an Elemental whose power and toughness equal your greatest creature power")
    void createsElementalEqualToGreatestControlledPower() {
        harness.setHand(player1, List.of(new TumbleweedRising()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent token = elemental(player1).orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Ignores creatures controlled by opponents")
    void ignoresOpponentCreatures() {
        harness.setHand(player1, List.of(new TumbleweedRising()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent token = elemental(player1).orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can be plotted and cast for free on a later turn")
    void plotsAndCastsLater() {
        TumbleweedRising rising = new TumbleweedRising();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(rising));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.castWithAlternateCost(player1, 0, List.of());

        assertThat(gd.plottedCardIds).contains(rising.getId());
        gd.turnNumber++;
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, rising.getId());
        harness.passBothPriorities();

        Permanent token = elemental(player1).orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("A 0/0 Elemental token dies when you control no creatures")
    void zeroSizedElementalDies() {
        harness.setHand(player1, List.of(new TumbleweedRising()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(elemental(player1)).isEmpty();
    }

    private Optional<Permanent> elemental(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Elemental"))
                .findFirst();
    }
}
