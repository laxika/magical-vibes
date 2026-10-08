package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.cards.r.RixMaadiDungeonPalace;
import com.github.laxika.magicalvibes.cards.s.StalkingVengeance;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WhiptailMoloch.class, MistralCharger.class, RixMaadiDungeonPalace.class,
        StalkingVengeance.class})
class WhiptailMolochTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 3 damage to target creature you control")
    void etbDealsThreeDamageToOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new StalkingVengeance());
        castWhiptailMoloch(target.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Whiptail Moloch");
    }

    @Test
    @DisplayName("ETB's 3 damage is lethal to a creature with three or less toughness")
    void etbDealsLethalDamageToSmallCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MistralCharger());
        castWhiptailMoloch(target.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mistral Charger");
        harness.assertInGraveyard(player1, "Mistral Charger");
    }

    @Test
    @DisplayName("ETB cannot target an opponent's creature")
    void etbCannotTargetOpponentCreature() {
        UUID opponentCreature = harness.addToBattlefieldAndReturn(player2, new StalkingVengeance()).getId();

        harness.setHand(player1, List.of(new WhiptailMoloch()));
        addWhiptailMana();

        assertThatThrownBy(() -> harness.getGameService().playCard(gd, player1, 0, 0, opponentCreature, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("ETB cannot target a noncreature permanent you control")
    void etbCannotTargetOwnNoncreaturePermanent() {
        UUID ownLand = harness.addToBattlefieldAndReturn(player1, new RixMaadiDungeonPalace()).getId();

        harness.setHand(player1, List.of(new WhiptailMoloch()));
        addWhiptailMana();

        assertThatThrownBy(() -> harness.getGameService().playCard(gd, player1, 0, 0, ownLand, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("ETB does nothing when its target leaves before resolution")
    void etbDoesNothingWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new StalkingVengeance());
        castWhiptailMoloch(target.getId());

        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Whiptail Moloch");
    }

    @Test
    @DisplayName("ETB must target itself when it is the only creature you control")
    void etbMustTargetItselfWhenAlone() {
        harness.castFromHand(player1, new WhiptailMoloch(), "{4}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Whiptail Moloch");
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Whiptail Moloch"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Whiptail Moloch");
        harness.assertInGraveyard(player1, "Whiptail Moloch");
    }

    @Test
    @DisplayName("ETB triggers when Whiptail Moloch enters without being cast")
    void etbTriggersWithoutBeingCast() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new StalkingVengeance());
        harness.enterBattlefieldAndReturn(player1, new WhiptailMoloch());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Whiptail Moloch");
    }

    @Test
    @DisplayName("ETB still deals damage after Whiptail Moloch leaves the battlefield")
    void etbResolvesAfterSourceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new StalkingVengeance());
        castWhiptailMoloch(target.getId());
        harness.passBothPriorities();

        Permanent source = findPermanent(player1, "Whiptail Moloch");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertInGraveyard(player1, "Whiptail Moloch");
    }

    private void castWhiptailMoloch(UUID targetId) {
        harness.setHand(player1, List.of(new WhiptailMoloch()));
        addWhiptailMana();
        harness.getGameService().playCard(gd, player1, 0, 0, targetId, null);
    }

    private void addWhiptailMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
