package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Themberchaud.class, Mountain.class, GiantSpider.class, AirElemental.class})
class ThemberchaudTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals damage based on Mountains to each player and other nonflying creature")
    void entersAndDamagesNonflyingCreaturesAndPlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        Permanent groundCreature = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent flyingCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        Permanent themberchaud = castThemberchaud();
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gameData.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(groundCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(flyingCreature.getMarkedDamage()).isZero();
        assertThat(themberchaud.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Attacking offers the exert may prompt")
    void attackTriggersExertPrompt() {
        addReadyThemberchaud();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Exerting gives flying until end of turn and skips the next untap")
    void exertGivesFlyingAndSkipsUntap() {
        Permanent themberchaud = addReadyThemberchaud();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.hasKeyword(gd, themberchaud, Keyword.FLYING)).isTrue();
        assertThat(themberchaud.isTapped()).isTrue();
        assertThat(themberchaud.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Declining exert does not give flying or skip the next untap")
    void decliningExertDoesNothing() {
        Permanent themberchaud = addReadyThemberchaud();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.hasKeyword(gd, themberchaud, Keyword.FLYING)).isFalse();
        assertThat(themberchaud.getSkipUntapCount()).isZero();
    }

    private Permanent castThemberchaud() {
        harness.setHand(player1, List.of(new Themberchaud()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        return gd.playerBattlefields.get(player1.getId()).getLast();
    }

    private Permanent addReadyThemberchaud() {
        return addCreatureReady(player1, new Themberchaud());
    }
}
