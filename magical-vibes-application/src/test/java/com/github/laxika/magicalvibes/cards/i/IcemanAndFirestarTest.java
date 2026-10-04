package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IcemanAndFirestar.class, Divination.class, Shock.class, GrizzlyBears.class, Forest.class})
class IcemanAndFirestarTest extends BaseCardTest {

    @Test
    @DisplayName("Blue spells tap up to one target creature")
    void blueSpellTapsTargetCreature() {
        Permanent icemanAndFirestar = harness.addToBattlefieldAndReturn(player1, new IcemanAndFirestar());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castDivination();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(icemanAndFirestar.getId(), target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Blue spell trigger rejects noncreature targets and may be declined")
    void blueSpellTriggerRejectsNoncreatureAndMayBeDeclined() {
        harness.addToBattlefield(player1, new IcemanAndFirestar());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        castDivination();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(target.getId()).doesNotContain(forest.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Red spells may discard a card to draw a card")
    void redSpellMayDiscardAndDraw() {
        harness.addToBattlefield(player1, new IcemanAndFirestar());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card spell = new Shock();
        Card discarded = new Forest();
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(spell, discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell, discarded);
    }

    @Test
    @DisplayName("Red spell trigger may be declined")
    void redSpellTriggerMayBeDeclined() {
        harness.addToBattlefield(player1, new IcemanAndFirestar());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card spell = new Shock();
        Card kept = new Forest();
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(spell, kept));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("The discard decision waits until the red trigger resolves")
    void discardDecisionWaitsUntilResolution() {
        harness.addToBattlefield(player1, new IcemanAndFirestar());
        harness.setHand(player1, List.of(new Shock(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("An empty hand cannot discard and therefore cannot draw")
    void emptyHandDoesNotDraw() {
        harness.addToBattlefield(player1, new IcemanAndFirestar());
        Card drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Green spells do not trigger either ability")
    void greenSpellDoesNotTrigger() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new IcemanAndFirestar());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(source.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's red spell does not trigger the discard ability")
    void opponentRedSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new IcemanAndFirestar());
        Card kept = new Forest();
        harness.setHand(player1, List.of(kept));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    private void castDivination() {
        harness.castFromHand(player1, new Divination(), "{2}{U}");
    }
}
