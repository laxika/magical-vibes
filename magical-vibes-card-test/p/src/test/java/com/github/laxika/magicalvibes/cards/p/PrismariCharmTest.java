package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrismariCharm.class, Forest.class, GiantSpider.class, Island.class, Spellbook.class,
        InvasionOfZendikar.class, JaceBeleren.class})
class PrismariCharmTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils 2, then draws a card")
    void surveilsThenDraws() {
        Card toGraveyard = new Forest();
        Card toTop = new Island();
        harness.setLibrary(player1, List.of(toGraveyard, toTop));
        harness.setHand(player1, List.of(new PrismariCharm()));
        addMana();

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerHands.get(player1.getId())).contains(toTop);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(toGraveyard);
    }

    @Test
    @DisplayName("Deals 1 damage to each of two any-targets")
    void damagesTwoTargets() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new PrismariCharm()));
        addMana();

        harness.castModalInstant(player1, 0, 1, List.of(spider.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(spider.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals 1 damage when only one target is chosen")
    void damagesOneTarget() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new PrismariCharm()));
        addMana();

        harness.castModalInstant(player1, 0, 1, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Returns a target nonland permanent to its owner's hand")
    void returnsNonlandPermanent() {
        harness.addToBattlefield(player2, new Spellbook());
        harness.setHand(player1, List.of(new PrismariCharm()));
        addMana();

        harness.castModalInstant(player1, 0, 2,
                List.of(harness.getPermanentId(player2, "Spellbook")));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Spellbook");
        harness.assertNotOnBattlefield(player2, "Spellbook");
    }

    @Test
    @DisplayName("Cannot return a land to its owner's hand")
    void cannotReturnLand() {
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new PrismariCharm()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 2,
                List.of(harness.getPermanentId(player2, "Island"))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland");
    }

    @Test
    void damagesBattle() {
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfZendikar());
        battle.setCounterCount(CounterType.DEFENSE, 3);
        harness.setHand(player1, List.of(new PrismariCharm()));
        addMana();

        harness.castModalInstant(player1, 0, 1, List.of(battle.getId()));
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Invasion of Zendikar");
    }

    @Test
    void damagesPlaneswalkerWithoutDamagingItsController() {
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 3);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new PrismariCharm()));
        addMana();

        harness.castModalInstant(player1, 0, 1, List.of(jace.getId()));
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertLife(player2, 20);
    }

    @Test
    void stillDamagesRemainingTargetWhenFirstTargetLeavesBattlefield() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new PrismariCharm()));
        addMana();

        harness.castModalInstant(player1, 0, 1, List.of(spider.getId(), player2.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, spider));
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    void cannotChooseSameDamageTargetTwice() {
        harness.setHand(player1, List.of(new PrismariCharm()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1,
                List.of(player2.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageModeRequiresAtLeastOneTarget() {
        harness.setHand(player1, List.of(new PrismariCharm()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotDamageNoncreatureArtifact() {
        Permanent spellbook = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        harness.setHand(player1, List.of(new PrismariCharm()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(spellbook.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canPutBothSurveilledCardsInGraveyardThenDrawNextCard() {
        Card first = new Forest();
        Card second = new Island();
        Card next = new Spellbook();
        harness.setLibrary(player1, List.of(first, second, next));
        harness.setHand(player1, List.of(new PrismariCharm()));
        addMana();

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(next);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void canReorderBothSurveilledCardsBeforeDrawing() {
        Card first = new Forest();
        Card second = new Island();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new PrismariCharm()));
        addMana();

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first);
        harness.assertInGraveyard(player1, "Prismari Charm");
    }

    @Test
    void returnsStolenPermanentToOwnerRatherThanController() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        gd.stolenCreatures.put(spider.getId(), player2.getId());
        harness.setHand(player1, List.of(new PrismariCharm()));
        harness.setHand(player2, List.of());
        addMana();

        harness.castModalInstant(player1, 0, 2, List.of(spider.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Giant Spider");
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(spider.getCard());
        harness.assertNotOnBattlefield(player1, "Giant Spider");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
