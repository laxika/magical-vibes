package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DauthiMercenary;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({AtKnifepoint.class, DauthiMercenary.class, GrizzlyBears.class, Shock.class})
class AtKnifepointTest extends BaseCardTest {

    @Test
    @DisplayName("Outlaws you control have first strike during your turn")
    void grantsFirstStrikeToOutlawsYouControlDuringYourTurn() {
        Permanent outlaw = harness.addToBattlefieldAndReturn(player1, new DauthiMercenary());
        Permanent nonOutlaw = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingOutlaw = harness.addToBattlefieldAndReturn(player2, new DauthiMercenary());
        harness.addToBattlefield(player1, new AtKnifepoint());

        assertThat(gqs.hasKeyword(gd, outlaw, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonOutlaw, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingOutlaw, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Outlaws lose the granted first strike during an opponent's turn")
    void removesFirstStrikeDuringOpponentsTurn() {
        Permanent outlaw = harness.addToBattlefieldAndReturn(player1, new DauthiMercenary());
        harness.addToBattlefield(player1, new AtKnifepoint());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThat(gqs.hasKeyword(gd, outlaw, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("A crime creates one Mercenary token and the token can boost a creature")
    void createsMercenaryTokenWithBoostAbility() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AtKnifepoint());
        commitCrime();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);

        Permanent mercenary = tokens.getFirst();
        mercenary.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int mercenaryIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);
        harness.activateAbility(player1, mercenaryIndex, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(mercenary.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The crime trigger fires only once each turn")
    void crimeTriggerFiresOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new AtKnifepoint());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        commitCrime();
        commitCrime();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isEqualTo(1);
    }

    private void commitCrime() {
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
    }
}
