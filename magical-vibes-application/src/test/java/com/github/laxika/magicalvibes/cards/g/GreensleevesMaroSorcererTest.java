package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PatronWizard;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GreensleevesMaroSorcerer.class, Forest.class, PatronWizard.class, ChandraNalaar.class})
class GreensleevesMaroSorcererTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the number of lands you control")
    void powerAndToughnessCountControlledLands() {
        Permanent greensleeves = addCreatureReady(player1, new GreensleevesMaroSorcerer());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        assertThat(gqs.getEffectivePower(gd, greensleeves)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, greensleeves)).isEqualTo(2);
    }

    @Test
    @DisplayName("Landfall creates a 3/3 green Badger token")
    void landfallCreatesBadger() {
        addCreatureReady(player1, new GreensleevesMaroSorcerer());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent badger = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.BADGER))
                .findFirst()
                .orElseThrow();
        assertThat(badger.getEffectivePower()).isEqualTo(3);
        assertThat(badger.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's land does not trigger landfall")
    void opponentLandDoesNotTriggerLandfall() {
        addCreatureReady(player1, new GreensleevesMaroSorcerer());
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);

        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.BADGER))
                .count()).isZero();
    }

    @Test
    @DisplayName("Protection from Wizards prevents a Wizard from blocking")
    void protectionFromWizardsPreventsBlocking() {
        Permanent greensleeves = addCreatureReady(player1, new GreensleevesMaroSorcerer());
        greensleeves.setAttacking(true);
        Permanent wizard = addCreatureReady(player2, new PatronWizard());

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, wizard), indexOf(player1, greensleeves)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from planeswalkers prevents a planeswalker's ability from targeting it")
    void protectionFromPlaneswalkersPreventsTargeting() {
        Permanent greensleeves = addCreatureReady(player1, new GreensleevesMaroSorcerer());
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 3);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(
                player2, indexOf(player2, chandra), 1, 2, greensleeves.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
