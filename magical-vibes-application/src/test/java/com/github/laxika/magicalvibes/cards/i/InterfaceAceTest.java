package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.ConquerorsGalleon;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InterfaceAceTest extends BaseCardTest {

    @Test
    @DisplayName("Uses toughness to crew a Vehicle")
    void usesToughnessToCrewVehicle() {
        addCreatureReady(player1, new InterfaceAce());
        Permanent vehicle = addVehicleReady(player1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()).get(0).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot crew a Vehicle with toughness below its crew value")
    void cannotCrewVehicleWithInsufficientToughness() {
        Permanent ace = addCreatureReady(player1, new InterfaceAce());
        TestCards.mutableCard(ace).setToughness(3);
        addVehicleReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    @DisplayName("Untaps itself once when it becomes tapped during your turn")
    void untapsOnceDuringOwnTurn() {
        Permanent ace = addCreatureReady(player1, new InterfaceAce());

        tapAndResolve(ace);

        assertThat(ace.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not trigger when it becomes tapped during an opponent's turn")
    void doesNotTriggerDuringOpponentsTurn() {
        Permanent ace = addCreatureReady(player1, new InterfaceAce());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        ace.tap();
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkEnchantedPermanentTapTriggers(gd, ace));

        assertThat(gd.stack).isEmpty();
        assertThat(ace.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Triggers only once each turn")
    void triggersOnlyOnceEachTurn() {
        Permanent ace = addCreatureReady(player1, new InterfaceAce());

        tapAndResolve(ace);
        ace.tap();
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkEnchantedPermanentTapTriggers(gd, ace));

        assertThat(gd.stack).isEmpty();
        assertThat(ace.isTapped()).isTrue();
    }

    private void tapAndResolve(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkEnchantedPermanentTapTriggers(gd, permanent));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }

    private Permanent addVehicleReady(Player player) {
        Permanent permanent = new Permanent(new ConquerorsGalleon());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
