package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CruelCelebrant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GodEternalBontu.class, Forest.class, Mountain.class, Plains.class,
        WrathOfGod.class, SwordsToPlowshares.class, CruelCelebrant.class})
class GodEternalBontuTest extends BaseCardTest {

    @Test
    @DisplayName("The enter-the-battlefield trigger sacrifices other permanents and draws that many cards")
    void enterTriggerSacrificesOtherPermanentsAndDraws() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Card drawn = new Plains();
        Card secondDrawn = new Plains();
        harness.setLibrary(player1, List.of(drawn, secondDrawn, new Plains()));
        harness.setHand(player1, List.of(new GodEternalBontu()));
        addBontuMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bontu = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(land.getId(), secondLand.getId());
        assertThat(choice.validIds()).doesNotContain(bontu.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(land.getId(), secondLand.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(bontu);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn, secondDrawn);
    }

    @Test
    @DisplayName("The death trigger may put God-Eternal Bontu third from the top")
    void deathTriggerPutsBontuThirdFromTop() {
        Card top = new Plains();
        Card second = new Mountain();
        Card third = new Forest();
        harness.setLibrary(player1, List.of(top, second, third));
        harness.addToBattlefield(player1, new GodEternalBontu());
        Card bontu = gd.playerBattlefields.get(player1.getId()).getFirst().getCard();

        destroyBontuWithWrath();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(top.getId(), second.getId(), bontu.getId(), third.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(bontu.getId()));
    }

    @Test
    @DisplayName("Declining the death trigger leaves God-Eternal Bontu in the graveyard")
    void decliningDeathTriggerLeavesBontuInGraveyard() {
        harness.addToBattlefield(player1, new GodEternalBontu());
        Card bontu = gd.playerBattlefields.get(player1.getId()).getFirst().getCard();

        destroyBontuWithWrath();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(bontu.getId()));
    }

    @Test
    @DisplayName("The exile trigger may put God-Eternal Bontu third from the top")
    void exileTriggerPutsBontuThirdFromTop() {
        Card top = new Plains();
        Card second = new Mountain();
        Card third = new Forest();
        harness.setLibrary(player1, List.of(top, second, third));
        Permanent bontuPermanent = harness.addToBattlefieldAndReturn(player1, new GodEternalBontu());
        Card bontu = bontuPermanent.getCard();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player2, 0, bontuPermanent.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(top.getId(), second.getId(), bontu.getId(), third.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(bontu.getId()));
    }

    @Test
    @DisplayName("Choosing zero sacrifices leaves the battlefield and library unchanged")
    void maySacrificeZeroPermanents() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Card top = new Plains();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new GodEternalBontu()));
        addBontuMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("With no other permanents, Bontu neither sacrifices itself nor draws")
    void noOtherPermanentsDrawsNothing() {
        Card top = new Plains();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new GodEternalBontu()));
        addBontuMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "God-Eternal Bontu");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    @DisplayName("Bontu goes to the bottom when its owner's library has only one card")
    void deathTriggerUsesBottomOfShortLibrary() {
        Card top = new Plains();
        harness.setLibrary(player1, List.of(top));
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new GodEternalBontu());
        Card bontu = permanent.getCard();

        destroyBontuWithWrath();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, bontu);
        harness.assertNotInGraveyard(player1, "God-Eternal Bontu");
    }

    @Test
    @DisplayName("Sacrificed Celebrants each see both simultaneous deaths")
    void sacrificesAreSimultaneousForDeathTriggers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CruelCelebrant());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CruelCelebrant());
        Card top = new Plains();
        Card next = new Forest();
        harness.setLibrary(player1, List.of(top, next, new Mountain()));
        harness.setHand(player1, List.of(new GodEternalBontu()));
        addBontuMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top, next);
        for (int i = 0; i < 4 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(first.getCard(), second.getCard());
    }

    @Test
    @DisplayName("Declining the exile trigger leaves Bontu in exile")
    void decliningExileTriggerLeavesBontuInExile() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new GodEternalBontu());
        Card top = new Plains();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, permanent.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(permanent.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        harness.assertNotOnBattlefield(player1, "God-Eternal Bontu");
    }

    private void addBontuMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void destroyBontuWithWrath() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
