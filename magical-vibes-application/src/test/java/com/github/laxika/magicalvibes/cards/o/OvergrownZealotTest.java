package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.p.PyrotechnicPerformer;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OvergrownZealot.class, PyrotechnicPerformer.class})
class OvergrownZealotTest extends BaseCardTest {

    @Test
    void firstAbilityAddsOneManaOfTheChosenColor() {
        addReadyZealot();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void secondAbilityAddsTwoManaOfTheChosenColorForTurningPermanentsFaceUp() {
        addReadyZealot();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId())
                .getTurnPermanentsFaceUpMana(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void secondAbilityCanPayToTurnAPermanentFaceUp() {
        Permanent zealot = addReadyZealot();
        harness.setHand(player1, List.of(new PyrotechnicPerformer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.castCreatureWithMorph(player1, 0);
            resolveAllTriggers();
        });

        Permanent performer = findPermanent(player1, "Pyrotechnic Performer");
        int zealotIndex = gd.playerBattlefields.get(player1.getId()).indexOf(zealot);
        harness.activateAbility(player1, zealotIndex, 1, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(performer));

        assertThat(performer.isFaceDown()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId())
                .getTurnPermanentsFaceUpMana(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void secondAbilityCannotPayForAMorphCost() {
        addReadyZealot();
        harness.setHand(player1, List.of(new PyrotechnicPerformer()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThatThrownBy(() -> harness.castCreatureWithMorph(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getTurnPermanentsFaceUpMana(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    void secondAbilityPaysBothGenericAndColoredCostsToTurnAManifestedCreatureFaceUp() {
        addReadyZealot();
        Permanent manifested = harness.addToBattlefieldAndReturn(player1, new OvergrownZealot());
        manifested.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        manifested.setManifested(true);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());
        harness.turnFaceUp(player1, 1);

        assertThat(manifested.isFaceDown()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId())
                .getTurnPermanentsFaceUpManaTotal()).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void secondAbilityCannotPayForAnOrdinaryCreatureSpell() {
        addReadyZealot();
        harness.setHand(player1, List.of(new OvergrownZealot()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getTurnPermanentsFaceUpMana(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void firstAbilityPaysTheTapCostAndPreventsUsingTheSecond() {
        Permanent zealot = addReadyZealot();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(zealot.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTurnPermanentsFaceUpManaTotal()).isZero();
    }

    @Test
    void failedFaceUpPaymentPreservesRestrictedManaAndTheFaceDownPermanent() {
        addReadyZealot();
        Permanent manifested = harness.addToBattlefieldAndReturn(player1, new OvergrownZealot());
        manifested.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        manifested.setManifested(true);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId())
                .getTurnPermanentsFaceUpMana(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    private Permanent addReadyZealot() {
        return addCreatureReady(player1, new OvergrownZealot());
    }
}
