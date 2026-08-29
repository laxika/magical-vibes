package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InquisitionOfKozilekTest extends BaseCardTest {

    @Test
    void castingTargetsAPlayer() {
        harness.setHand(player1, List.of(new InquisitionOfKozilek()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    void choosesOnlyNonlandCardsWithManaValueAtMostThree() {
        Card eligibleCreature = new GrizzlyBears();
        Card tooExpensive = new ColossalDreadmaw();
        Card land = new Forest();
        Card eligibleSpell = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(eligibleCreature, tooExpensive, land, eligibleSpell)));
        harness.setHand(player1, List.of(new InquisitionOfKozilek()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.validIndices()).containsExactly(0, 3);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(eligibleCreature);
        assertThat(gd.playerHands.get(player2.getId()))
                .containsExactlyInAnyOrder(tooExpensive, land, eligibleSpell);
    }

    @Test
    void doesNothingWhenTargetHasNoEligibleCards() {
        Card tooExpensive = new ColossalDreadmaw();
        Card land = new Forest();
        harness.setHand(player2, new ArrayList<>(List.of(tooExpensive, land)));
        harness.setHand(player1, List.of(new InquisitionOfKozilek()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(tooExpensive, land);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(tooExpensive, land);
    }
}
