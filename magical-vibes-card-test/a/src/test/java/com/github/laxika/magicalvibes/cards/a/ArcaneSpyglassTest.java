package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArcaneSpyglass.class, AuriokGlaivemaster.class, DarksteelCitadel.class, DarksteelIngot.class})
class ArcaneSpyglassTest extends BaseCardTest {

    @Test
    void sacrificesLandDrawsAndAddsChargeCounter() {
        Permanent spyglass = addReadySpyglass();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new DarksteelCitadel());
        DarksteelIngot drawnCard = new DarksteelIngot();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(spyglass.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(spyglass.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        harness.assertInGraveyard(player1, "Darksteel Citadel");
    }

    @Test
    void choosesWhichLandToSacrificeWhenMultipleAreAvailable() {
        Permanent spyglass = addReadySpyglass();
        Permanent keptLand = harness.addToBattlefieldAndReturn(player1, new DarksteelCitadel());
        Permanent sacrificedLand = harness.addToBattlefieldAndReturn(player1, new DarksteelCitadel());
        DarksteelIngot drawnCard = new DarksteelIngot();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, sacrificedLand.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(keptLand).doesNotContain(sacrificedLand);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(spyglass.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void removesThreeChargeCountersAndDraws() {
        Permanent spyglass = addReadySpyglass();
        spyglass.setCounterCount(CounterType.CHARGE, 4);
        DarksteelIngot drawnCard = new DarksteelIngot();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(spyglass.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(spyglass.isTapped()).isFalse();
    }

    @Test
    void cannotActivateFirstAbilityWithoutLand() {
        addReadySpyglass();
        Permanent nonland = harness.addToBattlefieldAndReturn(player1, new AuriokGlaivemaster());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(nonland);
    }

    @Test
    void cannotActivateFirstAbilityWithoutTwoGenericMana() {
        Permanent spyglass = addReadySpyglass();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new DarksteelCitadel());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(spyglass.isTapped()).isFalse();
        assertThat(spyglass.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void cannotActivateFirstAbilityWhenAlreadyTapped() {
        Permanent spyglass = addReadySpyglass();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new DarksteelCitadel());
        spyglass.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    void cannotActivateSecondAbilityWithFewerThanThreeChargeCounters() {
        Permanent spyglass = addReadySpyglass();
        spyglass.setCounterCount(CounterType.CHARGE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void firstAbilityPaysCostsBeforeDrawingAndAddingCounter() {
        Permanent spyglass = addReadySpyglass();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new DarksteelCitadel());
        DarksteelIngot drawnCard = new DarksteelIngot();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(spyglass.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        harness.assertInGraveyard(player1, "Darksteel Citadel");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(spyglass.getCounterCount(CounterType.CHARGE)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(spyglass.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void secondAbilityCanBeActivatedRepeatedlyWhileTappedAndPaysCountersImmediately() {
        Permanent spyglass = addReadySpyglass();
        spyglass.tap();
        spyglass.setCounterCount(CounterType.CHARGE, 6);
        DarksteelIngot firstCard = new DarksteelIngot();
        DarksteelIngot secondCard = new DarksteelIngot();
        harness.setLibrary(player1, List.of(firstCard, secondCard));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(spyglass.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(spyglass.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(firstCard, secondCard);
        assertThat(spyglass.isTapped()).isTrue();
    }

    private Permanent addReadySpyglass() {
        return addCreatureReady(player1, new ArcaneSpyglass());
    }
}
