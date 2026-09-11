package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NiambiEsteemedSpeaker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KeyToTheSideDoor.class, ArvadTheCursed.class, Forest.class, GrizzlyBears.class,
        NiambiEsteemedSpeaker.class})
class KeyToTheSideDoorTest extends BaseCardTest {

    @Test
    void makesTargetCreatureUnblockableUntilEndOfTurn() {
        Permanent key = addReadyKey(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(key.isTapped()).isTrue();
        assertThat(target.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    void discardsMatchingLegendaryCardAndDrawsTwoCards() {
        Permanent key = addReadyKey(player1);
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.setHand(player1, List.of(new ArvadTheCursed()));
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(0);

        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(key.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Arvad the Cursed");
        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Forest", "Grizzly Bears");
    }

    @Test
    void cannotDiscardAnUnrelatedLegendaryCard() {
        addReadyKey(player1);
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.setHand(player1, List.of(new NiambiEsteemedSpeaker()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotUseALegendaryPermanentControlledByAnOpponent() {
        addReadyKey(player1);
        harness.addToBattlefield(player2, new ArvadTheCursed());
        harness.setHand(player1, List.of(new ArvadTheCursed()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyKey(Player player) {
        Permanent key = new Permanent(new KeyToTheSideDoor());
        key.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(key);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return key;
    }
}
