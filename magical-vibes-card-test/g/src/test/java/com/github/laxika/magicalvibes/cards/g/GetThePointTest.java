package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CoralCommando;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({GetThePoint.class, CoralCommando.class, Forest.class})
class GetThePointTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target creature and offers scry 1")
    void destroysCreatureAndScries() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CoralCommando());

        castAndResolveAt(target);

        harness.assertNotOnBattlefield(player2, "Coral Commando");
        harness.assertInGraveyard(player2, "Coral Commando");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    @Test
    @DisplayName("Completing scry 1 finishes resolving Get the Point")
    void completingScryFinishesSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CoralCommando());
        Card top = gd.playerDecks.get(player1.getId()).getFirst();

        castAndResolveAt(target);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(top);
        harness.assertInGraveyard(player1, "Get the Point");
    }

    @Test
    @DisplayName("Scry 1 can put the top card on the bottom")
    void canPutScryCardOnBottom() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CoralCommando());
        Card top = new Forest();
        Card next = new CoralCommando();
        harness.setLibrary(player1, List.of(top, next));

        castAndResolveAt(target);

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.playerId()).isEqualTo(player1.getId());
        assertThat(scry.cards()).containsExactly(top);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, top);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Get the Point");
    }

    @Test
    @DisplayName("An illegal target prevents scrying")
    void doesNotScryWhenTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CoralCommando());
        Card top = new Forest();
        harness.setLibrary(player1, List.of(top));

        castAt(target);
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        harness.assertInGraveyard(player1, "Get the Point");
    }

    @Test
    @DisplayName("An empty library does not prevent destroying the creature")
    void resolvesWithEmptyLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CoralCommando());
        harness.setLibrary(player1, List.of());

        castAndResolveAt(target);

        harness.assertNotOnBattlefield(player2, "Coral Commando");
        harness.assertInGraveyard(player2, "Coral Commando");
        harness.assertInGraveyard(player1, "Get the Point");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new GetThePoint()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castAndResolveAt(Permanent target) {
        prepareSpell();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void castAt(Permanent target) {
        prepareSpell();
        harness.castInstant(player1, 0, target.getId());
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new GetThePoint()));
        addMana();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
