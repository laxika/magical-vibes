package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RazorGolem;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormCauldron.class, Forest.class, Plains.class, RazorGolem.class})
class StormCauldronTest extends BaseCardTest {

    @Test
    @DisplayName("Each player may play one additional land per turn")
    void raisesLandPlayLimitForEachPlayer() {
        harness.addToBattlefield(player1, new StormCauldron());

        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);
        assertThat(gd.getMaxLandsThisTurn(player2.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Two Storm Cauldrons grant two additional land plays")
    void twoStormCauldronsStack() {
        harness.addToBattlefield(player1, new StormCauldron());
        harness.addToBattlefield(player2, new StormCauldron());

        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(3);
        assertThat(gd.getMaxLandsThisTurn(player2.getId())).isEqualTo(3);
    }

    @Test
    @DisplayName("The return trigger waits on the stack before returning the tapped land")
    void returnTriggerWaitsBeforeReturningLand() {
        harness.addToBattlefield(player1, new StormCauldron());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.pendingManaAbilityTriggers).hasSize(1);

        resolveDeferredTriggers();

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("The mana produced by the tapped land remains in the pool")
    void manaRemainsAfterLandReturns() {
        harness.addToBattlefield(player1, new StormCauldron());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);
        resolveDeferredTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Returning a tapped land waits for the triggered ability to resolve")
    void landReturnWaitsForTriggerResolution() {
        harness.addToBattlefield(player1, new StormCauldron());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.pendingManaAbilityTriggers).hasSize(1);
    }

    @Test
    @DisplayName("Symmetric — an opponent's tapped land also returns to their hand")
    void symmetricReturnsOpponentsTappedLand() {
        harness.addToBattlefield(player1, new StormCauldron());
        harness.addToBattlefield(player2, new Forest());

        harness.tapPermanent(player2, 0);
        resolveDeferredTriggers();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("Affinity remains locked in while Plains are tapped to cast Razor Golem")
    void affinitySpellUsesPlainsBeforeReturnTriggersResolve() {
        harness.addToBattlefield(player1, new StormCauldron());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new Plains());
        }
        harness.setHand(player1, List.of(new RazorGolem()));

        harness.tapPermanent(player1, 1);
        harness.tapPermanent(player1, 2);
        harness.tapPermanent(player1, 3);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).anySatisfy(entry ->
                assertThat(entry.getCard().getName()).isEqualTo("Razor Golem"));

        resolveDeferredTriggers();

        harness.assertOnBattlefield(player1, "Razor Golem");
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Plains"))
                .hasSize(3);
    }

    @Test
    @DisplayName("Without Storm Cauldron a tapped land stays on the battlefield")
    void noReturnWithoutStormCauldron() {
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
    }

    private void resolveDeferredTriggers() {
        for (int i = 0; i < 10 && (!gd.stack.isEmpty() || !gd.pendingManaAbilityTriggers.isEmpty()); i++) {
            harness.passBothPriorities();
        }
    }
}
