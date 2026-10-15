package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkyfisherSpider.class, GrizzlyBears.class, Forest.class, WrathOfGod.class})
class SkyfisherSpiderTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may sacrifice another creature to destroy a target nonland permanent")
    void etbSacrificeDestroysTargetNonlandPermanent() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        SkyfisherSpider spiderCard = new SkyfisherSpider();
        harness.castFromHand(player1, spiderCard, "{2}{B}{G}");
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
        harness.castFromHand(player1, new SkyfisherSpider(), "{2}{B}{G}");
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

    @Test
    @DisplayName("A stolen Spider gains life from its controller's graveyard without exiling from its owner's")
    void stolenSpiderStaysInOwnersGraveyard() {
        SkyfisherSpider spiderCard = new SkyfisherSpider();
        spiderCard.setOwnerId(player2.getId());
        Permanent spider = harness.addToBattlefieldAndReturn(player1, spiderCard);
        gd.stolenCreatures.put(spider.getId(), player2.getId());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        int lifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        destroyWithWrathOfGod();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(spiderCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(spiderCard);
    }

    @Test
    @DisplayName("Death trigger counts simultaneous deaths but not noncreatures or opposing graveyards")
    void deathTriggerCountsOnlyControllersCreatureCards() {
        SkyfisherSpider spiderCard = new SkyfisherSpider();
        harness.addToBattlefield(player1, spiderCard);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        int lifeBefore = gd.getLife(player1.getId());

        destroyWithWrathOfGod();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spiderCard);
    }

    @Test
    @DisplayName("The destruction trigger can target Skyfisher Spider itself after the sacrifice")
    void reflexiveTriggerCanDestroySpiderItself() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        SkyfisherSpider spiderCard = new SkyfisherSpider();
        harness.castFromHand(player1, spiderCard, "{2}{B}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        Permanent spider = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(spiderCard.getId()))
                .findFirst().orElseThrow();
        PendingInteraction.PermanentChoice targetChoice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(targetChoice.validIds()).contains(spider.getId()).doesNotContain(sacrifice.getId());
        harness.handlePermanentChosen(player1, spider.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(spider);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spiderCard, sacrifice.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(spider);
    }
    private void destroyWithWrathOfGod() {
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
    }
}
