package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CaptainSisay;
import com.github.laxika.magicalvibes.cards.e.EiganjoCastle;
import com.github.laxika.magicalvibes.cards.k.KondasBanner;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.v.VancesBlastingCannons;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BountyAgent.class, CaptainSisay.class, KondasBanner.class, LeoninScimitar.class,
        VancesBlastingCannons.class, EiganjoCastle.class})
class BountyAgentTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and destroys a legendary creature")
    void sacrificesItselfAndDestroysLegendaryCreature() {
        addReadyAgent();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CaptainSisay());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bounty Agent");
        harness.assertInGraveyard(player2, "Captain Sisay");
    }

    @Test
    @DisplayName("Destroys a legendary artifact")
    void destroysLegendaryArtifact() {
        addReadyAgent();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KondasBanner());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Konda's Banner");
    }

    @Test
    @DisplayName("Destroys a legendary enchantment")
    void destroysLegendaryEnchantment() {
        addReadyAgent();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VancesBlastingCannons());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Vance's Blasting Cannons");
    }

    @Test
    @DisplayName("Cannot target a nonlegendary artifact")
    void cannotTargetNonlegendaryArtifact() {
        addReadyAgent();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a legendary land")
    void cannotTargetLegendaryLand() {
        addReadyAgent();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EiganjoCastle());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sacrificeIsPaidBeforeTargetIsDestroyed() {
        addReadyAgent();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CaptainSisay());

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertInGraveyard(player1, "Bounty Agent");
        harness.assertNotOnBattlefield(player1, "Bounty Agent");
        harness.assertOnBattlefield(player2, "Captain Sisay");
        harness.assertNotInGraveyard(player2, "Captain Sisay");

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Captain Sisay");
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new BountyAgent());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CaptainSisay());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Bounty Agent");
        harness.assertOnBattlefield(player2, "Captain Sisay");
        harness.assertNotInGraveyard(player1, "Bounty Agent");
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent agent = addReadyAgent();
        agent.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CaptainSisay());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Bounty Agent");
        harness.assertOnBattlefield(player2, "Captain Sisay");
    }

    @Test
    void canDestroyOwnLegendaryPermanent() {
        addReadyAgent();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CaptainSisay());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bounty Agent");
        harness.assertInGraveyard(player1, "Captain Sisay");
    }

    @Test
    void cannotTargetNonlegendaryCreature() {
        addReadyAgent();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BountyAgent());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Bounty Agent");
        harness.assertOnBattlefield(player2, "Bounty Agent");
    }

    @Test
    void vigilanceKeepsAgentUntappedWhenAttacking() {
        Permanent agent = addReadyAgent();

        declareAttackers(List.of(0));

        assertThat(agent.isTapped()).isFalse();
    }

    private Permanent addReadyAgent() {
        return addCreatureReady(player1, new BountyAgent());
    }
}
