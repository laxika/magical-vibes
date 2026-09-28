package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GhoulcallersBell;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RaulTroubleShooter.class, GhoulcallersBell.class, Shock.class, LightningBolt.class})
class RaulTroubleShooterTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability makes each player mill a card")
    void eachPlayerMillsACard() {
        Permanent raul = addReadyRaul();
        Card ownCard = new Shock();
        Card opponentCard = new LightningBolt();
        harness.setLibrary(player1, List.of(ownCard));
        harness.setLibrary(player2, List.of(opponentCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentCard);
        assertThat(raul.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can cast one spell milled this turn from the graveyard")
    void castsMilledSpellOnceDuringTurn() {
        addReadyRaul();
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(shock));
        harness.setLibrary(player2, List.of(new LightningBolt()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        prepareMainPhase();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromGraveyardTargeting(player1, 0, player2.getId());
           harness.passBothPriorities();

           assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
       }

    @Test
    @DisplayName("Cannot cast a card that was not milled this turn")
    void cannotCastCardNotMilledThisTurn() {
        addReadyRaul();
        harness.setGraveyard(player1, List.of(new Shock()));
        prepareMainPhase();

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be cast from graveyard");
    }

    @Test
    @DisplayName("The permission can be used only once during each turn")
    void permissionIsOncePerTurn() {
        addReadyRaul();
        Permanent bell = harness.addToBattlefieldAndReturn(player1, new GhoulcallersBell());
        bell.setSummoningSick(false);
        Card first = new Shock();
        Card second = new Shock();
        harness.setLibrary(player1, List.of(first, second));
        harness.setLibrary(player2, List.of(new LightningBolt(), new LightningBolt()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        prepareMainPhase();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castFromGraveyardTargeting(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyRaul() {
        Permanent raul = harness.addToBattlefieldAndReturn(player1, new RaulTroubleShooter());
        raul.setSummoningSick(false);
        return raul;
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
