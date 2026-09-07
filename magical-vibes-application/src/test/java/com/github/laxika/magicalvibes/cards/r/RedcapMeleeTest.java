package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RedcapMelee.class, GrizzlyBears.class, Mountain.class, RagingRedcap.class})
class RedcapMeleeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage to a nonred creature and sacrifices a land")
    void dealsDamageToNonredCreatureAndSacrificesLand() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, java.util.List.of(new RedcapMelee()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not sacrifice a land when the damaged creature is red")
    void doesNotSacrificeLandForRedCreature() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new RagingRedcap());
        harness.setHand(player1, java.util.List.of(new RedcapMelee()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Raging Redcap"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertInGraveyard(player2, "Raging Redcap");
    }

    @Test
    @DisplayName("Rejects a noncreature, nonplaneswalker target")
    void rejectsInvalidTarget() {
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, java.util.List.of(new RedcapMelee()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player1, "Mountain")))
                .isInstanceOf(IllegalStateException.class);
    }
}
