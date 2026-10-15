package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuspensionField.class, AirElemental.class, GrizzlyBears.class, Naturalize.class, SummitProwler.class})
class SuspensionFieldTest extends BaseCardTest {

    private void castAndResolveSuspensionField(UUID targetId, boolean exileTarget) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SuspensionField(), "{1}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, exileTarget);
    }

    @Test
    @DisplayName("ETB may exile target creature with toughness 3 or greater")
    void etbExilesToughCreature() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castAndResolveSuspensionField(elemental.getId(), true);

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(elemental.getCard().getId()));
    }

    @Test
    @DisplayName("ETB may decline to exile the target")
    void etbMayDecline() {
        harness.addToBattlefield(player2, new AirElemental());
        UUID elementalId = harness.getPermanentId(player2, "Air Elemental");

        castAndResolveSuspensionField(elementalId, false);

        harness.assertOnBattlefield(player2, "Air Elemental");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Air Elemental"));
    }

    @Test
    @DisplayName("Cannot target a creature with toughness less than 3")
    void cannotTargetLowToughnessCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SuspensionField()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("toughness 3 or greater");
    }

    @Test
    @DisplayName("Exiled creature returns when Suspension Field leaves")
    void exiledCreatureReturnsWhenSourceLeaves() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        castAndResolveSuspensionField(elemental.getId(), true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID fieldId = harness.getPermanentId(player1, "Suspension Field");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, fieldId);

        harness.assertOnBattlefield(player2, "Air Elemental");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Air Elemental"));
    }

    @Test
    void exilesOwnCreatureWithExactlyThreeToughness() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SummitProwler());

        castAndResolveSuspensionField(target.getId(), true);

        harness.assertNotOnBattlefield(player1, "Summit Prowler");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    void entersWithoutEligibleCreatures() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SuspensionField(), "{1}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Suspension Field");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void sourceLeavingBeforeTriggerResolvesPreventsExile() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SummitProwler());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SuspensionField(), "{1}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        UUID fieldId = harness.getPermanentId(player1, "Suspension Field");
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0, fieldId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Suspension Field");
        harness.assertOnBattlefield(player2, "Summit Prowler");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
