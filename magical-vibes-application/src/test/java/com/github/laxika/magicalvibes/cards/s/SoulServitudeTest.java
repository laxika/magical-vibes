package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoulServitude.class, Forest.class, GrizzlyBears.class})
class SoulServitudeTest extends BaseCardTest {

    @Test
    void targetPlayerSacrificesAnyNontokenCreatureAndDiscardConjuresItsDuplicate() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, new ArrayList<>(List.of(new SoulServitude(), new Forest())));
        cast();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(bears.getCard());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class))
                .isNotNull();
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        Card duplicate = gd.playerHands.get(player1.getId()).getFirst();
        assertThat(duplicate.getName()).isEqualTo(bears.getCard().getName());
        assertThat(duplicate.getId()).isNotEqualTo(bears.getCard().getId());
        assertThat(gd.perpetualAnyColorManaForCastCardIds).contains(duplicate.getId());
    }

    @Test
    void decliningDiscardDoesNotConjure() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Forest forest = new Forest();
        harness.setHand(player1, new ArrayList<>(List.of(new SoulServitude(), forest)));
        cast();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
    }

    @Test
    void canTargetTheController() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, new ArrayList<>(List.of(new SoulServitude(), new Forest())));
        cast(player1.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears.getCard());
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    void conjuresDuringTheDiscardAbilityResolution() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SoulServitude(), new Forest()));
        cast();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sacrificeCreatesDiscardTriggerEvenWhenControllersHandIsEmpty() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SoulServitude()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(bears.getCard());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void noCreatureMeansNoDiscardOrDuplicate() {
        Forest forest = new Forest();
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new SoulServitude(), forest));

        cast();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void targetedPlayerChoosesWhichNontokenCreatureToSacrifice() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SoulServitude(), new Forest()));
        cast();

        harness.handlePermanentChosen(player2, second.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(second.getCard())
                .doesNotContain(first.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first).doesNotContain(second);
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    void duplicateCanBeCastUsingOnlyBlackMana() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SoulServitude(), new Forest()));
        cast();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        Card duplicate = gd.playerHands.get(player1.getId()).getFirst();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard().getId()).isEqualTo(duplicate.getId()));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void cast() {
        cast(player2.getId());
    }

    private void cast(java.util.UUID targetPlayerId) {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, targetPlayerId);
        resolveAllTriggers();
    }
}
