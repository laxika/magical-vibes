package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SkyfisherSpiderTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may sacrifice another creature to destroy a target nonland permanent")
    void etbSacrificeDestroysTargetNonlandPermanent() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        SkyfisherSpider spiderCard = new SkyfisherSpider();
        harness.setHand(player1, List.of(spiderCard));
        addSkyfisherMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice sacrificeChoice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(sacrificeChoice.validIds()).containsExactly(sacrifice.getId());
        harness.handlePermanentChosen(player1, sacrifice.getId());

        PendingInteraction.PermanentChoice targetChoice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(targetChoice.validIds()).contains(target.getId()).doesNotContain(land.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrifice.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(spiderCard.getId()));
    }

    @Test
    @DisplayName("Declining the ETB sacrifice leaves permanents unchanged")
    void decliningEtbSacrificeDoesNothing() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SkyfisherSpider()));
        addSkyfisherMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sacrifice);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Death trigger gains life for creature cards in the graveyard and exiles Skyfisher Spider")
    void deathTriggerGainsLifeAndExilesSource() {
        SkyfisherSpider spiderCard = new SkyfisherSpider();
        harness.addToBattlefield(player1, spiderCard);
        Card graveyardCreature1 = new GrizzlyBears();
        Card graveyardCreature2 = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCreature1, graveyardCreature2));
        int lifeBefore = gd.getLife(player1.getId());

        destroyWithWrathOfGod();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spiderCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(spiderCard);
    }

    @Test
    @DisplayName("Declining the death trigger gains no life and does not exile Skyfisher Spider")
    void decliningDeathTriggerDoesNothing() {
        SkyfisherSpider spiderCard = new SkyfisherSpider();
        harness.addToBattlefield(player1, spiderCard);
        int lifeBefore = gd.getLife(player1.getId());

        destroyWithWrathOfGod();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(spiderCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spiderCard);
    }

    private void addSkyfisherMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private void destroyWithWrathOfGod() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
    }
}
