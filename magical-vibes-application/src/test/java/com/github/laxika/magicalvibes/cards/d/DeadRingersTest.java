package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BogWraith;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeadRingers.class, GiantSpider.class, GrizzlyBears.class, HillGiant.class, BogWraith.class})
class DeadRingersTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys two nonblack creatures with exactly matching colors")
    void destroysCreaturesWithMatchingColors() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        castDeadRingers(List.of(
                harness.getPermanentId(player2, "Grizzly Bears"),
                harness.getPermanentId(player2, "Giant Spider")));

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Does nothing when the targets have different colors")
    void doesNothingWhenColorsDiffer() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        castDeadRingers(List.of(
                harness.getPermanentId(player2, "Grizzly Bears"),
                harness.getPermanentId(player2, "Hill Giant")));

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Does nothing when one target leaves before resolution")
    void doesNothingWhenOneTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID spiderId = harness.getPermanentId(player2, "Giant Spider");
        harness.setHand(player1, List.of(new DeadRingers()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castSorcery(player1, 0, List.of(bearsId, spiderId));

        gd.playerBattlefields.get(player2.getId()).removeIf(permanent -> permanent.getId().equals(bearsId));

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        harness.addToBattlefield(player2, new BogWraith());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DeadRingers()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(
                harness.getPermanentId(player2, "Bog Wraith"),
                harness.getPermanentId(player2, "Grizzly Bears"))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack");
    }

    private void castDeadRingers(List<UUID> targets) {
        harness.setHand(player1, List.of(new DeadRingers()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castSorcery(player1, 0, targets);
        harness.passBothPriorities();
    }
}
