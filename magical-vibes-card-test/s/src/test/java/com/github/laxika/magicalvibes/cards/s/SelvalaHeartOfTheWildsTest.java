package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SelvalaHeartOfTheWilds.class, HillGiant.class, Forest.class, Terror.class})
class SelvalaHeartOfTheWildsTest extends BaseCardTest {

    @Test
    @DisplayName("The entering creature's controller may draw when it has uniquely greatest power")
    void enteringCreatureControllerMayDraw() {
        harness.addToBattlefield(player1, new SelvalaHeartOfTheWilds());
        harness.setLibrary(player2, List.of(new Forest()));

        harness.enterBattlefieldAndReturn(player2, new HillGiant());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("A tied greatest power does not draw")
    void tiedGreatestPowerDoesNotDraw() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new SelvalaHeartOfTheWilds());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.enterBattlefieldAndReturn(player1, new HillGiant());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Adds mana equal to the greatest power in any combination of colors")
    void addsGreatestPowerInAnyCombinationOfColors() {
        harness.addToBattlefield(player1, new HillGiant());
        Permanent selvala = harness.addToBattlefieldAndReturn(player1, new SelvalaHeartOfTheWilds());
        selvala.setSummoningSick(false);
        harness.addMana(player1, ManaColor.GREEN, 1);

        int selvalaIndex = gd.playerBattlefields.get(player1.getId()).indexOf(selvala);
        harness.activateAbility(player1, selvalaIndex, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());
        harness.handleListChoice(player1, ManaColor.BLUE.name());
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("A qualifying creature's controller may decline the draw")
    void enteringCreatureControllerMayDecline() {
        harness.addToBattlefield(player1, new SelvalaHeartOfTheWilds());
        harness.setLibrary(player2, List.of(new Forest()));
        int handBefore = gd.playerHands.get(player2.getId()).size();

        harness.enterBattlefieldAndReturn(player2, new HillGiant());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Uses last known power when the entering creature leaves before resolution")
    void departedEnteringCreatureMayStillDraw() {
        harness.addToBattlefield(player1, new SelvalaHeartOfTheWilds());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Terror()));
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new HillGiant());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, entering.getId());
        harness.assertInGraveyard(player1, "Hill Giant");
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Compares against opposing creatures as the ability resolves")
    void opposingGreaterPowerPreventsDraw() {
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        opposing.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addToBattlefield(player1, new SelvalaHeartOfTheWilds());
        harness.setLibrary(player1, List.of(new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.enterBattlefieldAndReturn(player1, new HillGiant());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("A creature tied on entry can qualify by gaining power before resolution")
    void evaluatesPowerAtResolution() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new SelvalaHeartOfTheWilds());
        harness.setLibrary(player1, List.of(new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new HillGiant());
        entering.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Selvala does not trigger for her own entry")
    void doesNotTriggerForOwnEntry() {
        harness.enterBattlefieldAndReturn(player1, new SelvalaHeartOfTheWilds());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Selvala counts her own power and ignores opposing power for mana")
    void usesOwnPowerAndIgnoresOpponentForMana() {
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        opposing.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent selvala = harness.addToBattlefieldAndReturn(player1, new SelvalaHeartOfTheWilds());
        selvala.setSummoningSick(false);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(selvala.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, ManaColor.WHITE.name());
        harness.handleListChoice(player1, ManaColor.WHITE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Zero greatest power adds no mana but still pays the activation cost")
    void zeroPowerAddsNoMana() {
        Permanent selvala = harness.addToBattlefieldAndReturn(player1, new SelvalaHeartOfTheWilds());
        selvala.setSummoningSick(false);
        selvala.setPowerModifier(-2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(selvala.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
