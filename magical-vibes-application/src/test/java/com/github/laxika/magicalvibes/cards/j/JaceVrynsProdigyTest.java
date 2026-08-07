package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JaceVrynsProdigyTest extends BaseCardTest {

    @Test
    @DisplayName("{T} loots but leaves Jace untransformed while the graveyard is too small")
    void lootsWithoutTransformingBelowFiveCards() {
        Permanent jace = addJace(player1);
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player1, new ArrayList<>());

        harness.activateAbility(player1, indexOf(player1, jace), null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Shock");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Jace, Vryn's Prodigy");
        harness.assertNotOnBattlefield(player1, "Jace, Telepath Unbound");
    }

    @Test
    @DisplayName("The discarded card counts toward the five, so Jace returns transformed with loyalty")
    void transformsWhenGraveyardReachesFiveCards() {
        Permanent jace = addJace(player1);
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player1, filler(4));

        harness.activateAbility(player1, indexOf(player1, jace), null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertNotOnBattlefield(player1, "Jace, Vryn's Prodigy");
        harness.assertOnBattlefield(player1, "Jace, Telepath Unbound");

        Permanent walker = findPermanent(player1, "Jace, Telepath Unbound");
        assertThat(walker.isTransformed()).isTrue();
        assertThat(walker.getCounterCount(CounterType.LOYALTY)).isPositive();
    }

    private List<Card> filler(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Shock());
        }
        return cards;
    }

    private Permanent addJace(Player player) {
        Permanent perm = new Permanent(new JaceVrynsProdigy());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
