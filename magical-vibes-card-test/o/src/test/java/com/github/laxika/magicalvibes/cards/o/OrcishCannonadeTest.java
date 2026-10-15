package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.h.HavenwoodWurm;
import com.github.laxika.magicalvibes.cards.s.Snapback;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrcishCannonade.class, HavenwoodWurm.class, Snapback.class})
class OrcishCannonadeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to a creature, deals 3 damage to its controller, and draws a card")
    void damagesCreatureDamagesControllerAndDrawsCard() {
        harness.addToBattlefield(player2, new HavenwoodWurm());
        harness.setHand(player1, List.of(new OrcishCannonade()));
        harness.setLibrary(player1, List.of(new HavenwoodWurm()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Havenwood Wurm"));

        assertThat(findPermanent(player2, "Havenwood Wurm").getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 17);
        harness.assertInHand(player1, "Havenwood Wurm");
    }

    @Test
    @DisplayName("Deals 2 damage to a player, deals 3 damage to the caster, and draws a card")
    void damagesPlayerDamagesCasterAndDrawsCard() {
        harness.setHand(player1, List.of(new OrcishCannonade()));
        harness.setLibrary(player1, List.of(new HavenwoodWurm()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 18);
        harness.assertInHand(player1, "Havenwood Wurm");
    }

    @Test
    @DisplayName("Targeting yourself deals a total of 5 damage and draws exactly one card")
    void targetingSelfDealsFiveDamageAndDrawsOneCard() {
        harness.setHand(player1, List.of(new OrcishCannonade()));
        harness.setLibrary(player1, List.of(new HavenwoodWurm(), new Snapback()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 20);
        harness.assertInHand(player1, "Havenwood Wurm");
        harness.assertNotInHand(player1, "Snapback");
        harness.assertInGraveyard(player1, "Orcish Cannonade");
    }

    @Test
    @DisplayName("An illegal target prevents both the self damage and the draw")
    void removedTargetPreventsAllEffects() {
        harness.addToBattlefield(player2, new HavenwoodWurm());
        harness.setHand(player1, List.of(new OrcishCannonade()));
        harness.setHand(player2, List.of(new Snapback()));
        harness.setLibrary(player1, List.of(new HavenwoodWurm()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        var targetId = harness.getPermanentId(player2, "Havenwood Wurm");

        harness.castInstant(player1, 0, targetId);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertNotInHand(player1, "Havenwood Wurm");
        harness.assertInHand(player2, "Havenwood Wurm");
        harness.assertInGraveyard(player1, "Orcish Cannonade");
    }
}
