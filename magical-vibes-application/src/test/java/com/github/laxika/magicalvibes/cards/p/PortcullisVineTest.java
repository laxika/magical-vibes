package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.o.OnakkeOgre;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PortcullisVine.class, OnakkeOgre.class})
class PortcullisVineTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Portcullis Vine draws a card")
    void sacrificesSourceAndDrawsCard() {
        addCreatureReady(player1, new PortcullisVine());
        harness.setLibrary(player1, List.of(new OnakkeOgre(), new OnakkeOgre()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Portcullis Vine");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Only a creature with defender may be sacrificed")
    void nonDefenderCreatureIsNotSacrificed() {
        addCreatureReady(player1, new PortcullisVine());
        harness.addToBattlefield(player1, new OnakkeOgre());
        harness.setLibrary(player1, List.of(new OnakkeOgre(), new OnakkeOgre()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Portcullis Vine");
        harness.assertOnBattlefield(player1, "Onakke Ogre");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Another tapped defender can be sacrificed before the card is drawn")
    void sacrificesAnotherDefenderAsCost() {
        var vine = addCreatureReady(player1, new PortcullisVine());
        var otherVine = harness.addToBattlefieldAndReturn(player1, new PortcullisVine());
        otherVine.setTapped(true);
        otherVine.setSummoningSick(true);
        harness.setLibrary(player1, List.of(new OnakkeOgre(), new OnakkeOgre()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, otherVine.getId());

        assertThat(vine.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(vine).doesNotContain(otherVine);
        harness.assertInGraveyard(player1, "Portcullis Vine");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Onakke Ogre");
    }

    @Test
    @DisplayName("A summoning-sick Vine cannot activate its tap ability")
    void cannotActivateWhileSummoningSick() {
        var vine = harness.addToBattlefieldAndReturn(player1, new PortcullisVine());
        vine.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Portcullis Vine");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Vine cannot activate its tap ability")
    void cannotActivateWhileTapped() {
        var vine = addCreatureReady(player1, new PortcullisVine());
        vine.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Portcullis Vine");
        assertThat(gd.stack).isEmpty();
    }
}
