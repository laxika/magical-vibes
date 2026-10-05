package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArabaMothrider;
import com.github.laxika.magicalvibes.cards.s.ScrollOfOrigins;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoltingSkin.class, ArabaMothrider.class, ScrollOfOrigins.class})
class MoltingSkinTest extends BaseCardTest {

    @Test
    @DisplayName("The return cost is paid before the regeneration ability resolves")
    void paysReturnCostBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ArabaMothrider());
        harness.addToBattlefield(player1, new MoltingSkin());

        harness.activateAbility(player1, 1, null, target.getId());

        harness.assertInHand(player1, "Molting Skin");
        harness.assertNotOnBattlefield(player1, "Molting Skin");
        assertThat(target.getRegenerationShield()).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(target.getRegenerationShield()).isEqualTo(1);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The regeneration shield prevents only the next destruction and taps the creature")
    void regenerationReplacesOnlyOneDestruction() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArabaMothrider());
        harness.addToBattlefield(player1, new MoltingSkin());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.inMutationScope(() ->
                assertThat(harness.getPermanentRemovalService().tryDestroyPermanent(gd, target)).isFalse());

        harness.assertOnBattlefield(player2, "Araba Mothrider");
        harness.assertNotInGraveyard(player2, "Araba Mothrider");
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getRegenerationShield()).isZero();

        harness.inMutationScope(() ->
                assertThat(harness.getPermanentRemovalService().tryDestroyPermanent(gd, target)).isTrue());

        harness.assertNotOnBattlefield(player2, "Araba Mothrider");
        harness.assertInGraveyard(player2, "Araba Mothrider");
    }

    @Test
    @DisplayName("Returning Molting Skin to its owner's hand regenerates the target creature")
    void returnsSelfAndRegeneratesTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArabaMothrider());
        harness.addToBattlefield(player1, new MoltingSkin());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Molting Skin");
        assertThat(target.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Molting Skin can target only creatures")
    void cannotTargetNonCreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ScrollOfOrigins());
        harness.addToBattlefield(player1, new MoltingSkin());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Returning a controlled Molting Skin goes to its owner's hand")
    void returnsControlledSourceToItsOwnersHand() {
        MoltingSkin moltingSkin = new MoltingSkin();
        moltingSkin.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, moltingSkin);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ArabaMothrider());

        harness.activateAbility(player2, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Molting Skin");
        harness.assertNotInHand(player2, "Molting Skin");
        assertThat(target.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability does not regenerate a target that leaves before resolution")
    void targetLeavingBeforeResolutionFulfillsNoRegeneration() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArabaMothrider());
        harness.addToBattlefield(player1, new MoltingSkin());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Molting Skin");
        assertThat(target.getRegenerationShield()).isZero();
    }
}
