package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FierceWitchstalker.class, Gingerbrute.class})
class FierceWitchstalkerTest extends BaseCardTest {

    @Test
    void enteringCreatesFoodToken() {
        harness.setHand(player1, List.of(new FierceWitchstalker()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(harness.getPermanentId(player1, "Food")).isNotNull();
    }

    @Test
    void foodIsSacrificedAsACostAndLifeIsGainedOnResolution() {
        castWitchstalker();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);

        assertThat(countPermanents(player1, "Food")).isZero();
        harness.assertLife(player1, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        resolveAllTriggers();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Fierce Witchstalker");
    }

    @Test
    void foodCannotBeActivatedWithoutTwoMana() {
        castWitchstalker();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        assertThat(findPermanent(player1, "Food").isTapped()).isFalse();
        harness.assertLife(player1, 20);
    }

    @Test
    void tappedFoodCannotBeActivated() {
        castWitchstalker();
        findPermanent(player1, "Food").tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        harness.assertLife(player1, 20);
    }

    @Test
    void enteringTriggerCreatesFoodEvenAfterWitchstalkerLeaves() {
        harness.setHand(player1, List.of(new FierceWitchstalker()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fierce Witchstalker");
        assertThat(countPermanents(player1, "Food")).isZero();
        harness.getPermanentRemovalService().removePermanentToGraveyard(
                gd, findPermanent(player1, "Fierce Witchstalker"));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
    }

    @Test
    void opponentCreatesAndUsesTheirOwnFood() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new FierceWitchstalker()));
        harness.addMana(player2, ManaColor.GREEN, 4);
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(countPermanents(player2, "Food")).isEqualTo(1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 1, null, null);
        resolveAllTriggers();

        harness.assertLife(player2, 23);
        harness.assertLife(player1, 20);
    }

    @Test
    void trampleDealsExcessDamageThroughABlocker() {
        Permanent attacker = addCreatureReady(player1, new FierceWitchstalker());
        Permanent blocker = addCreatureReady(player2, new Gingerbrute());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 1, player2.getId(), 3));

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player2, "Gingerbrute");
        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Fierce Witchstalker");
    }

    private void castWitchstalker() {
        harness.setHand(player1, List.of(new FierceWitchstalker()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
