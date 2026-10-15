package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BleedDry;
import com.github.laxika.magicalvibes.cards.m.MassiveMight;
import com.github.laxika.magicalvibes.cards.s.SporebackWolf;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HalanaAndAlenaPartners.class, SporebackWolf.class})
class HalanaAndAlenaPartnersTest extends BaseCardTest {

    @Test
    void putsCountersEqualToItsPowerOnAnotherCreatureAndGivesItHaste() {
        Permanent partners = harness.addToBattlefieldAndReturn(player1, new HalanaAndAlenaPartners());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SporebackWolf());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(partners.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    void cannotTargetItselfOrAnOpponentsCreature() {
        Permanent partners = harness.addToBattlefieldAndReturn(player1, new HalanaAndAlenaPartners());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SporebackWolf());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new SporebackWolf());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, partners.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void hasteWearsOffAtEndOfTurnButCountersRemain() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SporebackWolf());
        harness.addToBattlefield(player1, new HalanaAndAlenaPartners());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
    }

    @Test
    void doesNotTriggerDuringAnOpponentsCombat() {
        harness.addToBattlefield(player1, new HalanaAndAlenaPartners());
        harness.addToBattlefield(player1, new SporebackWolf());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed(MassiveMight.class)
    void usesPowerAtResolutionAfterAnInstantBoost() {
        Permanent partners = harness.addToBattlefieldAndReturn(player1, new HalanaAndAlenaPartners());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SporebackWolf());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.setHand(player1, List.of(new MassiveMight()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, partners.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {-2, -3})
    void grantsHasteWithoutCountersWhenSourcePowerIsZeroOrNegative(int powerModifier) {
        Permanent partners = harness.addToBattlefieldAndReturn(player1, new HalanaAndAlenaPartners());
        partners.setPowerModifier(powerModifier);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SporebackWolf());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    @CardUsed(BleedDry.class)
    void usesLastKnownNegativePowerWhenSourceIsExiledBeforeResolution() {
        Permanent partners = harness.addToBattlefieldAndReturn(player1, new HalanaAndAlenaPartners());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SporebackWolf());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.setHand(player1, List.of(new BleedDry()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, partners.getId());
        harness.assertNotOnBattlefield(player1, "Halana and Alena, Partners");
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    @CardUsed(BleedDry.class)
    void doesNotGrantHasteWhenTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new HalanaAndAlenaPartners());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SporebackWolf());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.setHand(player1, List.of(new BleedDry()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.assertNotOnBattlefield(player1, "Sporeback Wolf");
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getGrantedKeywords()).doesNotContain(Keyword.HASTE);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotRequestATargetWhenNoOtherCreatureIsControlled() {
        harness.addToBattlefield(player1, new HalanaAndAlenaPartners());
        harness.addToBattlefield(player2, new SporebackWolf());

        advanceToCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
    }
}
