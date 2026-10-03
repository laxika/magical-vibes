package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.p.PlatypusBear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoilingRockRioter.class, PlatypusBear.class})
class BoilingRockRioterTest extends BaseCardTest {

    @Test
    void attackingAddsRedManaUntilEndOfCombat() {
        addRioterReady();

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void firebendingAndAllyCastingTriggerSeparately() {
        Permanent rioter = addRioterReady();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(gd.stack.stream()
                .filter(entry -> rioter.getId().equals(entry.getSourcePermanentId())))
                .hasSize(2);
    }

    @Test
    void tapsAnAllyToExileTargetCardFromAGraveyard() {
        Permanent rioter = addRioterReady();
        Card target = new PlatypusBear();
        harness.setGraveyard(player2, List.of(target));

        harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(rioter.isTapped()).isTrue();
        harness.assertNotInGraveyard(player2, "Platypus-Bear");
        assertThat(gd.getCardsExiledByPermanent(rioter.getId())).containsExactly(target);
    }

    @Test
    void attackingOffersCastingAnOwnedAllyDuringTriggerResolution() {
        Permanent rioter = addRioterReady();
        Card ownAlly = new BoilingRockRioter();
        Card ownNonAlly = new PlatypusBear();
        Card opponentAlly = new BoilingRockRioter();
        gd.addToExile(player1.getId(), ownAlly, rioter.getId());
        gd.addToExile(player1.getId(), ownNonAlly, rioter.getId());
        gd.addToExile(player2.getId(), opponentAlly, rioter.getId());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(harness.getCastingPermissionService().getCastableExiledCardIds(gd, player1.getId()))
                .doesNotContain(ownAlly.getId(), ownNonAlly.getId(), opponentAlly.getId());
    }

    @Test
    void canTapSummoningSickRioterToExileFromItsControllersGraveyard() {
        Permanent rioter = harness.addToBattlefieldAndReturn(player1, new BoilingRockRioter());
        Card target = new BoilingRockRioter();
        harness.setGraveyard(player1, List.of(target));

        harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(rioter.isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Boiling Rock Rioter");
        assertThat(gd.getCardsExiledByPermanent(rioter.getId())).containsExactly(target);
    }

    @Test
    void cannotPayTapCostWithANonAlly() {
        Permanent rioter = addRioterReady();
        rioter.tap();
        addCreatureReady(player1, new PlatypusBear());
        Card target = new BoilingRockRioter();
        harness.setGraveyard(player2, List.of(target));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player2, "Boiling Rock Rioter");
        assertThat(gd.getCardsExiledByPermanent(rioter.getId())).isEmpty();
    }

    @Test
    void doesNotExileATargetThatLeavesTheGraveyardBeforeResolution() {
        Permanent rioter = addRioterReady();
        Card target = new PlatypusBear();
        harness.setGraveyard(player2, List.of(target));

        harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.setGraveyard(player2, List.of());
        harness.setHand(player2, List.of(target));
        harness.passBothPriorities();

        assertThat(rioter.isTapped()).isTrue();
        harness.assertInHand(player2, "Platypus-Bear");
        assertThat(gd.getCardsExiledByPermanent(rioter.getId())).isEmpty();
    }

    private Permanent addRioterReady() {
        return addCreatureReady(player1, new BoilingRockRioter());
    }
}
