package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BearTrap;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RaggedPlaymate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VengefulPossession.class, Forest.class, RaggedPlaymate.class, BearTrap.class})
class VengefulPossessionTest extends BaseCardTest {

    @Test
    @DisplayName("Gains control, untaps, grants haste, and offers a discard-and-draw")
    void resolvesAndAcceptsLoot() {
        Permanent target = addCreature(player2);
        target.tap();
        Card toDiscard = new RaggedPlaymate();
        prepareLibrary();
        harness.setHand(player1, List.of(new VengefulPossession(), toDiscard));
        addMana();

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Ragged Playmate");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Declining the optional discard does not draw")
    void declinesLoot() {
        Permanent target = addCreature(player2);
        Card toKeep = new RaggedPlaymate();
        prepareLibrary();
        harness.setHand(player1, List.of(new VengefulPossession(), toKeep));
        addMana();

        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Ragged Playmate");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Control and haste expire at cleanup")
    void controlAndHasteExpireAtCleanup() {
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new VengefulPossession()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent creature = addCreature(player2);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BearTrap());
        harness.setHand(player1, List.of(new VengefulPossession()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("An illegal target prevents the optional discard and draw")
    void illegalTargetPreventsLoot() {
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new VengefulPossession(), new RaggedPlaymate()));
        prepareLibrary();
        addMana();

        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Vengeful Possession");
        harness.assertInHand(player1, "Ragged Playmate");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("An empty hand cannot produce a draw even when the optional action is accepted")
    void emptyHandDoesNotDraw() {
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new VengefulPossession()));
        prepareLibrary();
        addMana();

        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can untap and grant haste to a creature already controlled by the caster")
    void canTargetOwnCreature() {
        Permanent target = addCreature(player1);
        target.tap();
        harness.setHand(player1, List.of(new VengefulPossession()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    private Permanent addCreature(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new RaggedPlaymate());
        creature.setSummoningSick(false);
        return creature;
    }

    private void prepareLibrary() {
        harness.setLibrary(player1, List.of(new Forest()));
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
