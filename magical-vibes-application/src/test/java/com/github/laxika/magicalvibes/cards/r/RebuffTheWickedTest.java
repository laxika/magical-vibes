package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RebuffTheWicked.class, GiantGrowth.class, GrizzlyBears.class, LlanowarElves.class, Shock.class})
class RebuffTheWickedTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell that targets a permanent you control")
    void countersSpellTargetingYourPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        var targetId = harness.getPermanentId(player2, "Grizzly Bears");

        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.setHand(player2, List.of(new RebuffTheWicked()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, shock.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shock");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Rebuff the Wicked");
    }

    @Test
    @DisplayName("Cannot target a spell that targets an opponent's permanent")
    void cannotTargetSpellTargetingOpponentsPermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        var targetId = harness.getPermanentId(player1, "Grizzly Bears");

        GiantGrowth growth = new GiantGrowth();
        harness.setHand(player1, List.of(growth));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new RebuffTheWicked()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, growth.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a spell that does not target a permanent")
    void cannotTargetNonTargetingSpell() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new RebuffTheWicked()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, elves.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
