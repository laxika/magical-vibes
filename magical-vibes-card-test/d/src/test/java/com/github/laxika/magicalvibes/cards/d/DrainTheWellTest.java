package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrainTheWell.class, Forest.class, GrizzlyBears.class, Boomerang.class})
class DrainTheWellTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target land and controller gains 2 life")
    void destroysLandAndGainsLife() {
        harness.addToBattlefield(player2, new Forest());
        UUID land = harness.getPermanentId(player2, "Forest");
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.setHand(player1, List.of(new DrainTheWell()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player1, 0, List.of(land));

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        harness.assertLife(player1, lifeBefore + 2);
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonland() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID creature = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new DrainTheWell()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(creature)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can destroy the caster's own land using green mana")
    void destroysOwnLandAndGainsLife() {
        harness.addToBattlefield(player1, new Forest());
        UUID land = harness.getPermanentId(player1, "Forest");
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new DrainTheWell()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, List.of(land));

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertLife(player1, 12);
    }

    @Test
    @DisplayName("Gains no life when the only target leaves before resolution")
    void gainsNoLifeWhenTargetLeaves() {
        harness.addToBattlefield(player2, new Forest());
        UUID land = harness.getPermanentId(player2, "Forest");
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new DrainTheWell(), new Boomerang()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, List.of(land));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, land);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInHand(player2, "Forest");
        harness.assertInGraveyard(player1, "Drain the Well");
        harness.assertLife(player1, 10);
    }
}
