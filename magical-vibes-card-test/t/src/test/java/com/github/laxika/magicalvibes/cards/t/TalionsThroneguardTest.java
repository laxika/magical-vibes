package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TalionsThroneguard.class, DarksteelRelic.class, GrizzlyBears.class, Island.class, Shock.class})
class TalionsThroneguardTest extends BaseCardTest {

    @Test
    void withoutBargainReturnsPermanentWithoutIncreasingItsCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TalionsThroneguard()));
        addTalionMana(4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void bargainReturnsNonlandPermanentAndTaxesItPerpetually() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TalionsThroneguard()));
        addTalionMana(4);

        castBargained(target.getId(), sacrifice.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Darksteel Relic");

        harness.setHand(player2, List.of(target.getCard()));
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bargainCanReturnAspellAndTaxItsPhysicalCard() {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        UUID shockId = gd.stack.getFirst().getCard().getId();

        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        harness.setHand(player1, List.of(new TalionsThroneguard()));
        addTalionMana(4);
        castBargained(shockId, sacrifice.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Shock");
        harness.assertNotInGraveyard(player2, "Shock");
        harness.addMana(player2, ManaColor.RED, 2);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetALandPermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        harness.setHand(player1, List.of(new TalionsThroneguard()));
        addTalionMana(4);

        assertThatThrownBy(() -> castBargained(land.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    void cannotBargainBySacrificingANontokenLand() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TalionsThroneguard()));
        addTalionMana(4);

        assertThatThrownBy(() -> castBargained(target.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Island");
    }

    @Test
    void canBargainBySacrificingANonartifactNonenchantmentCreatureToken() {
        GrizzlyBears token = new GrizzlyBears();
        token.setToken(true);
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, token);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TalionsThroneguard()));
        addTalionMana(4);

        castBargained(target.getId(), sacrifice.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    void bargainedCreatureCanBeCastWithoutChoosingABounceTarget() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        harness.setHand(player1, List.of(new TalionsThroneguard()));
        addTalionMana(4);

        harness.castKickedCreatureWithPermanent(player1, 0, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Talion's Throneguard");
        harness.assertInGraveyard(player1, "Darksteel Relic");
    }

    private void castBargained(UUID targetId, UUID sacrificeId) {
        harness.ensurePriority(player1);
        gs.playCard(gd, player1, 0, 0, targetId, null,
                List.of(), List.of(), false, sacrificeId, null, null, null, null, true);
    }

    private void addTalionMana(int amount) {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, amount - 2);
    }
}
