package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BasilicaStalker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AgencyCoroner.class, BasilicaStalker.class})
class AgencyCoronerTest extends BaseCardTest {

    @Test
    void sacrificesAnotherCreatureAndDrawsOneCard() {
        addReadyCoroner();
        harness.addToBattlefield(player1, new BasilicaStalker());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertInGraveyard(player1, "Basilica Stalker");
    }

    @Test
    void suspectedSacrificeDrawsTwoCards() {
        addReadyCoroner();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BasilicaStalker());
        bears.setSuspected(true);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        harness.assertInGraveyard(player1, "Basilica Stalker");
    }

    @Test
    void cannotSacrificeAgencyCoronerItself() {
        addReadyCoroner();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyCoroner() {
        return addCreatureReady(player1, new AgencyCoroner());
    }

    @Test
    void tappedSummoningSickCoronerCanActivate() {
        Permanent coroner = harness.addToBattlefieldAndReturn(player1, new AgencyCoroner());
        coroner.setSummoningSick(true);
        coroner.tap();
        harness.addToBattlefield(player1, new BasilicaStalker());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Basilica Stalker");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    void cannotSacrificeOpponentsCreature() {
        addReadyCoroner();
        harness.addToBattlefield(player2, new BasilicaStalker());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void suspectedCoronerDoesNotIncreaseDrawForUnsuspectedSacrifice() {
        addReadyCoroner().setSuspected(true);
        harness.addToBattlefield(player1, new BasilicaStalker());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    void laterUnsuspectedSacrificeDoesNotReuseEarlierSuspectedStatus() {
        addReadyCoroner();
        harness.addToBattlefieldAndReturn(player1, new BasilicaStalker()).setSuspected(true);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);

        harness.addToBattlefield(player1, new BasilicaStalker());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
    }
}
