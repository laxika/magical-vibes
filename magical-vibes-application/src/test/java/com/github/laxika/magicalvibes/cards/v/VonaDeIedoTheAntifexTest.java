package com.github.laxika.magicalvibes.cards.v;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VonaDeIedoTheAntifex.class, Forest.class, GrizzlyBears.class})
class VonaDeIedoTheAntifexTest extends BaseCardTest {

    @Test
    void destroysTargetAndDiscardingConjuresAUsableDuplicate() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, new ArrayList<>(List.of(new VonaDeIedoTheAntifex(), new Forest())));
        addMana();

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        Card duplicate = gd.playerHands.get(player1.getId()).getFirst();
        assertThat(duplicate.getName()).isEqualTo(target.getCard().getName());
        assertThat(duplicate.getId()).isNotEqualTo(target.getCard().getId());
        assertThat(gd.perpetualAnyColorManaForCastCardIds).contains(duplicate.getId());
    }

    @Test
    void decliningDiscardDoesNotConjure() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Forest forest = new Forest();
        harness.setHand(player1, new ArrayList<>(List.of(new VonaDeIedoTheAntifex(), forest)));
        addMana();

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
    }

    @Test
    void cannotTargetOwnPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new VonaDeIedoTheAntifex()));
        addMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
