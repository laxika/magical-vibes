package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SelvalaHeartOfTheWilds.class, HillGiant.class, Forest.class})
class SelvalaHeartOfTheWildsTest extends BaseCardTest {

    @Test
    void enteringCreatureControllerMayDrawWhenItIsStrictlyLargest() {
        harness.addToBattlefield(player1, new SelvalaHeartOfTheWilds());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest()));

        harness.enterBattlefieldAndReturn(player2, new HillGiant());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotDrawWhenAnotherCreatureTiesAtResolution() {
        harness.addToBattlefield(player1, new SelvalaHeartOfTheWilds());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest()));

        harness.enterBattlefieldAndReturn(player2, new HillGiant());
        harness.passBothPriorities();
        addCreatureReady(player1, new HillGiant());
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void usesEnteringCreaturePowerWhenItLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new SelvalaHeartOfTheWilds());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest()));

        Permanent entering = harness.enterBattlefieldAndReturn(player2, new HillGiant());
        gd.playerBattlefields.get(player2.getId()).remove(entering);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    void tapsAndPaysGreenToAddManaEqualToGreatestControlledCreaturePower() {
        Permanent selvala = addCreatureReady(player1, new SelvalaHeartOfTheWilds());
        addCreatureReady(player1, new HillGiant());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, selvala.getId());
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(3);
        assertThat(selvala.isTapped()).isTrue();
    }
}
