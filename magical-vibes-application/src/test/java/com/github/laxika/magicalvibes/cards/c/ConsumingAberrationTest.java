package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConsumingAberrationTest extends BaseCardTest {

    private Permanent aberration() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> "Consuming Aberration".equals(p.getCard().getName()))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Power and toughness equal the number of cards in opponents' graveyards")
    void powerToughnessMatchesOpponentGraveyards() {
        harness.addToBattlefield(player1, new ConsumingAberration());

        assertThat(gqs.getEffectivePower(gd, aberration())).isZero();
        assertThat(gqs.getEffectiveToughness(gd, aberration())).isZero();

        harness.setGraveyard(player2, List.of(new Forest(), new GrizzlyBears(), new Divination()));

        assertThat(gqs.getEffectivePower(gd, aberration())).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, aberration())).isEqualTo(3);
    }

    @Test
    @DisplayName("Cards in the controller's own graveyard do not count")
    void ownGraveyardDoesNotCount() {
        harness.addToBattlefield(player1, new ConsumingAberration());
        harness.setGraveyard(player1, List.of(new Forest(), new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, aberration())).isZero();
    }

    @Test
    @DisplayName("Casting a spell makes each opponent mill until they reveal a land")
    void spellCastMillsOpponentUntilLand() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new ConsumingAberration());

        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(
                new GrizzlyBears(),
                new Divination(),
                new Forest(),      // land -> stop
                new GrizzlyBears() // stays in library
        ));

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting("name")
                .containsExactlyInAnyOrder("Grizzly Bears", "Divination", "Forest");
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting("name").containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("The mill trigger grows the Aberration, since the milled cards land in an opponent's graveyard")
    void millFeedsItsOwnPowerToughness() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new ConsumingAberration());
        // Keeps it above 0/0 so state-based actions don't bin it before the trigger resolves.
        harness.setGraveyard(player2, List.of(new Divination()));

        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(
                new GrizzlyBears(),
                new Forest()
        ));

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, aberration())).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, aberration())).isEqualTo(3);
    }
}
