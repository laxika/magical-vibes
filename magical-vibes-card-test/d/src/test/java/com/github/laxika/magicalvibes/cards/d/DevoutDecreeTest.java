package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FeralAbomination;
import com.github.laxika.magicalvibes.cards.c.ChandraNovicePyromancer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.g.GoblinBirdGrabber;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.l.LeylineOfCombustion;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DevoutDecree.class, FeralAbomination.class, ChandraNovicePyromancer.class, Forest.class,
        GreenwoodSentinel.class, Mountain.class, LeylineOfCombustion.class, GoblinBirdGrabber.class})
class DevoutDecreeTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling a black creature is followed by scry 1")
    void exilesBlackCreatureAndScries() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FeralAbomination());
        Card top = new Forest();
        harness.setLibrary(player1, List.of(top, new Mountain()));
        resolveAt(target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(top);
    }

    @Test
    @DisplayName("A red planeswalker is a legal target")
    void exilesRedPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraNovicePyromancer());
        target.setCounterCount(CounterType.LOYALTY, 5);
        resolveAt(target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("A non-black non-red creature cannot be targeted")
    void cannotTargetGreenCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());

        assertThatThrownBy(() -> castAt(target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A red noncreature nonplaneswalker cannot be targeted")
    void cannotTargetRedEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeylineOfCombustion());

        assertThatThrownBy(() -> castAt(target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Scry can put the top card on the bottom after exiling your own red creature")
    void exilesOwnRedCreatureAndBottomsScryCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GoblinBirdGrabber());
        Card top = new Forest();
        Card next = new Mountain();
        harness.setLibrary(player1, List.of(top, next));
        resolveAt(target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target.getCard());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, top);
        harness.assertInGraveyard(player1, "Devout Decree");
    }

    @Test
    @DisplayName("Exile still happens when the caster's library is empty")
    void exilesWithEmptyLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FeralAbomination());
        harness.setLibrary(player1, List.of());
        resolveAt(target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Devout Decree");
    }

    @Test
    @DisplayName("No scry happens when the only target leaves before resolution")
    void doesNotScryWhenTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FeralAbomination());
        Card top = new Forest();
        Card next = new Mountain();
        harness.setLibrary(player1, List.of(top, next));
        castAt(target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, next);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(target.getCard());
        harness.assertInGraveyard(player1, "Devout Decree");
    }

    private void resolveAt(UUID targetId) {
        prepareSpell();
        harness.castAndResolveSorcery(player1, 0, targetId);
    }

    private void castAt(UUID targetId) {
        prepareSpell();
        harness.castSorcery(player1, 0, targetId);
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new DevoutDecree()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
