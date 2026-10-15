package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StickFearlessMentor.class, Forest.class, LightningBolt.class})
class StickFearlessMentorTest extends BaseCardTest {

    @Test
    void controlledSourceDamageExilesTopCardAndGrantsPlayPermissionUntilNextTurn() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new StickFearlessMentor());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        harness.castFromExile(player1, topCard.getId());
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void abilityTriggersOnlyOnceEachTurn() {
        Forest firstTopCard = new Forest();
        Forest secondTopCard = new Forest();
        harness.setLibrary(player1, List.of(firstTopCard, secondTopCard));
        harness.addToBattlefield(player1, new StickFearlessMentor());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(firstTopCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondTopCard);
    }

    @Test
    void damageFromOpponentSourceDoesNotTrigger() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new StickFearlessMentor());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void controlledSourceDamageToOpponentDoesNotTrigger() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new StickFearlessMentor());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void exiledSpellMustBePaidForAndCanBeCast() {
        LightningBolt topCard = new LightningBolt();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new StickFearlessMentor());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castFromExile(player1, topCard.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void permissionSurvivesInterveningTurnAndExpiresAfterControllersNextTurn() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.addToBattlefield(player1, new StickFearlessMentor());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }
}
