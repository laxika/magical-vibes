package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.cards.t.TravelingPhilosopher;
import com.github.laxika.magicalvibes.cards.v.VoyagesEnd;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CavalryPegasus.class, TravelingPhilosopher.class, BronzeSable.class, VoyagesEnd.class})
class CavalryPegasusTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking Humans gain flying, but non-Humans and nonattacking Humans do not")
    void attackingHumansGainFlying() {
        addCreatureReady(player1, new CavalryPegasus());
        Permanent attackingHuman = addCreatureReady(player1, new TravelingPhilosopher());
        Permanent attackingNonHuman = addCreatureReady(player1, new BronzeSable());
        Permanent nonattackingHuman = addCreatureReady(player1, new TravelingPhilosopher());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(attackingHuman.getGrantedKeywords()).contains(Keyword.FLYING);
        assertThat(attackingNonHuman.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
        assertThat(nonattackingHuman.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("Granted flying wears off at end of turn")
    void grantedFlyingWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new CavalryPegasus());
        Permanent attackingHuman = addCreatureReady(player1, new TravelingPhilosopher());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(attackingHuman.getGrantedKeywords()).contains(Keyword.FLYING);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(attackingHuman.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("Humans do not gain flying when the Pegasus does not attack")
    void pegasusMustAttackToTrigger() {
        addCreatureReady(player1, new CavalryPegasus());
        Permanent human = addCreatureReady(player1, new TravelingPhilosopher());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(human.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("The attack trigger resolves after the Pegasus leaves the battlefield")
    void triggerResolvesWithoutPegasus() {
        Permanent pegasus = addCreatureReady(player1, new CavalryPegasus());
        Permanent human = addCreatureReady(player1, new TravelingPhilosopher());
        VoyagesEnd bounce = new VoyagesEnd();
        harness.setHand(player2, List.of(bounce));
        harness.setLibrary(player2, List.of());

        declareAttackers(player1, List.of(0, 1));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, pegasus.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Cavalry Pegasus");
        assertThat(human.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Humans entering after resolution do not gain flying")
    void laterHumansDoNotGainFlying() {
        addCreatureReady(player1, new CavalryPegasus());
        Permanent attackingHuman = addCreatureReady(player1, new TravelingPhilosopher());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();
        Permanent laterHuman = harness.enterBattlefieldAndReturn(player1, new TravelingPhilosopher());

        assertThat(attackingHuman.getGrantedKeywords()).contains(Keyword.FLYING);
        assertThat(laterHuman.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }
}
