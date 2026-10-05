package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.c.CravenHulk;
import com.github.laxika.magicalvibes.cards.d.DemonBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Quakebringer.class, HillGiant.class, CravenHulk.class, DemonBolt.class})
class QuakebringerTest extends BaseCardTest {

    @Test
    @DisplayName("Opponents can't gain life while Quakebringer is on the battlefield")
    void opponentsCantGainLife() {
        harness.addToBattlefield(player1, new Quakebringer());

        assertThat(gqs.canPlayerGainLife(gd, player1.getId())).isTrue();
        assertThat(gqs.canPlayerGainLife(gd, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("Battlefield upkeep trigger deals 2 damage even without another Giant")
    void battlefieldUpkeepTriggerDealsDamageWithoutAnotherGiant() {
        harness.addToBattlefield(player1, new Quakebringer());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Graveyard upkeep trigger deals 2 damage while its controller controls a Giant")
    void graveyardUpkeepTriggerDealsDamageWithGiant() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setGraveyard(player1, List.of(new Quakebringer()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Graveyard upkeep trigger does not trigger without a Giant")
    void graveyardUpkeepTriggerDoesNotTriggerWithoutGiant() {
        harness.setGraveyard(player1, List.of(new Quakebringer()));

        advanceToUpkeep(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void graveyardTriggerStillDealsDamageAfterLastGiantDies() {
        harness.addToBattlefield(player1, new CravenHulk());
        harness.setGraveyard(player1, List.of(new Quakebringer()));
        harness.setHand(player2, List.of(new DemonBolt()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.addMana(player2, ManaColor.RED, 3);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Craven Hulk"));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Craven Hulk");
        resolveAllTriggers();

        harness.assertLife(player2, 18);
    }

    @Test
    void opponentGiantDoesNotEnableGraveyardTrigger() {
        harness.addToBattlefield(player2, new CravenHulk());
        harness.setGraveyard(player1, List.of(new Quakebringer()));

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void battlefieldTriggerStillDealsDamageAfterQuakebringerDies() {
        harness.addToBattlefield(player1, new Quakebringer());
        harness.setHand(player2, List.of(new DemonBolt()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.addMana(player2, ManaColor.RED, 3);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Quakebringer"));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Quakebringer");
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(gqs.canPlayerGainLife(gd, player2.getId())).isTrue();
    }

    @Test
    void eachGraveyardCopyTriggersIndependently() {
        harness.addToBattlefield(player1, new CravenHulk());
        harness.setGraveyard(player1, List.of(new Quakebringer(), new Quakebringer()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
    }

    @Test
    void doesNotTriggerOnOpponentsUpkeep() {
        harness.addToBattlefield(player1, new Quakebringer());
        harness.setGraveyard(player1, List.of(new Quakebringer()));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void graveyardQuakebringerDoesNotPreventLifeGain() {
        harness.setGraveyard(player1, List.of(new Quakebringer()));

        assertThat(gqs.canPlayerGainLife(gd, player1.getId())).isTrue();
        assertThat(gqs.canPlayerGainLife(gd, player2.getId())).isTrue();
    }

    @Test
    void foretellsAndCastsForFourManaOnLaterTurn() {
        Quakebringer card = new Quakebringer();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.findExiledCard(card.getId()).faceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Quakebringer");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gqs.canPlayerGainLife(gd, player2.getId())).isFalse();
    }
}
