package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BogWraith;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.t.TouchOfDarkness;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RecklessSpite.class, GrizzlyBears.class, HillGiant.class, BogWraith.class, TouchOfDarkness.class})
class RecklessSpiteTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys both targeted nonblack creatures and its controller loses 5 life")
    void destroysBothTargetsAndLosesFive() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new RecklessSpite()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.setLife(player1, 20);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");

        harness.castAndResolveInstant(player1, 0, List.of(bearsId, giantId));

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("Life loss still happens when one target is no longer on the battlefield")
    void losesLifeEvenWhenTargetGone() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new RecklessSpite()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.setLife(player1, 20);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");

        harness.castInstant(player1, 0, List.of(bearsId, giantId));

        // One target leaves before resolution — the life loss still happens.
        gd.playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getId().equals(giantId));

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("Does not destroy a target that becomes black before resolution")
    void doesNotDestroyTargetThatBecomesBlackBeforeResolution() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new RecklessSpite()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.setLife(player1, 20);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");

        harness.castInstant(player1, 0, List.of(bearsId, giantId));

        harness.setHand(player2, List.of(new TouchOfDarkness()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, giantId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("Does not resolve or cause life loss when both targets become black")
    void doesNotResolveWhenBothTargetsBecomeBlackBeforeResolution() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new RecklessSpite()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.setLife(player1, 20);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");

        harness.castInstant(player1, 0, List.of(bearsId, giantId));

        harness.setHand(player2, List.of(new TouchOfDarkness()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, List.of(bearsId, giantId));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Reckless Spite");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        harness.addToBattlefield(player2, new BogWraith());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RecklessSpite()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID blackId = harness.getPermanentId(player2, "Bog Wraith");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(blackId, bearsId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack");
    }

    @Test
    @DisplayName("Cannot be cast with only one target")
    void requiresTwoTargets() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RecklessSpite()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bearsId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose the same creature for both targets")
    void requiresDistinctTargets() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RecklessSpite()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bearsId, bearsId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
