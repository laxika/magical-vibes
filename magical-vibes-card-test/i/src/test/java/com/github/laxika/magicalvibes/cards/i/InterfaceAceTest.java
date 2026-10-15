package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BrightfieldGlider;
import com.github.laxika.magicalvibes.cards.l.LumberingWorldwagon;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InterfaceAce.class, LumberingWorldwagon.class, BrightfieldGlider.class})
class InterfaceAceTest extends BaseCardTest {

    @Test
    @DisplayName("Uses toughness to crew a Vehicle")
    void usesToughnessToCrewVehicle() {
        addCreatureReady(player1, new InterfaceAce());
        Permanent vehicle = addCreatureReady(player1, new LumberingWorldwagon());

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()).get(0).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot crew a Vehicle with toughness below its crew value")
    void cannotCrewVehicleWithInsufficientToughness() {
        Permanent ace = addCreatureReady(player1, new InterfaceAce());
        TestCards.mutableCard(ace).setToughness(3);
        addCreatureReady(player1, new LumberingWorldwagon());

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

    @Test
    void usesToughnessToSaddleMountWhileSummoningSick() {
        Permanent ace = harness.addToBattlefieldAndReturn(player1, new InterfaceAce());
        ace.setSummoningSick(true);
        Permanent mount = addCreatureReady(player1, new BrightfieldGlider());

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(mount.isSaddled()).isTrue();
        assertThat(ace.isTapped()).isFalse();
    }

    @Test
    void canCrewAgainAfterUntappingButOnlyUntapsOnce() {
        Permanent ace = addCreatureReady(player1, new InterfaceAce());
        Permanent vehicle = addCreatureReady(player1, new LumberingWorldwagon());

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();
        assertThat(ace.isTapped()).isFalse();

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(ace.isTapped()).isTrue();
    }

    @Test
    void tappingAnotherCreatureDoesNotConsumeUntapTrigger() {
        Permanent ace = addCreatureReady(player1, new InterfaceAce());
        Permanent other = addCreatureReady(player1, new BrightfieldGlider());

        other.tap();
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkEnchantedPermanentTapTriggers(gd, other));
        assertThat(gd.stack).isEmpty();

        tapAndResolve(ace);

        assertThat(ace.isTapped()).isFalse();
        assertThat(other.isTapped()).isTrue();
    }

    @Test
    void eachCopyHasItsOwnOncePerTurnUntap() {
        Permanent first = addCreatureReady(player1, new InterfaceAce());
        Permanent second = addCreatureReady(player1, new InterfaceAce());

        tapAndResolve(first);
        tapAndResolve(second);

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
    }

    private void tapAndResolve(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkEnchantedPermanentTapTriggers(gd, permanent));
        resolveAllTriggers();
    }

}
