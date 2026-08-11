package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmptinessTest extends BaseCardTest {

    @Test
    @DisplayName("White mana ETB returns a creature card with mana value 3 or less")
    void whiteBranchReturnsSmallCreature() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Emptiness()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Emptiness");
    }

    @Test
    @DisplayName("Black mana ETB puts three -1/-1 counters on up to one target creature")
    void blackBranchPutsCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new Emptiness()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Emptiness");
    }

    @Test
    @DisplayName("Evoke uses the hybrid alternate cost and sacrifices Emptiness after its ETB")
    void evokeReturnsCreatureAndSacrificesSelf() {
        Card creature = new LlanowarElves();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Emptiness()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreatureWithEvoke(player1, 0, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        harness.assertNotOnBattlefield(player1, "Emptiness");
        harness.assertInGraveyard(player1, "Emptiness");
    }

    @Test
    @DisplayName("Black branch rejects a noncreature permanent target")
    void blackBranchRequiresCreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new Emptiness()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
