package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.t.TormentingVoice;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HashatonScarabsFist.class, TormentingVoice.class, GrizzlyBears.class, Swamp.class})
class HashatonScarabsFistTest extends BaseCardTest {

    @Test
    void payingForDiscardedCreatureCreatesTappedZombieCopyAndLeavesCardInGraveyard() {
        harness.addToBattlefield(player1, new HashatonScarabsFist());
        Card discarded = new GrizzlyBears();
        harness.setHand(player1, List.of(new TormentingVoice(), discarded));
        harness.setLibrary(player1, List.of(new Swamp(), new Swamp()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorceryWithDiscard(player1, 0, 1);
        resolveUntilMayChoice();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .anyMatch(p -> p.isTapped()
                        && p.getCard().getPower() == 4
                        && p.getCard().getToughness() == 4
                        && p.getCard().getColor() == CardColor.BLACK
                        && p.getCard().getSubtypes().contains(CardSubtype.ZOMBIE));
    }

    @Test
    void decliningDoesNotCreateToken() {
        harness.addToBattlefield(player1, new HashatonScarabsFist());
        harness.setHand(player1, List.of(new TormentingVoice(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Swamp(), new Swamp()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorceryWithDiscard(player1, 0, 1);
        resolveUntilMayChoice();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken() && p.getCard().getSubtypes().contains(CardSubtype.ZOMBIE));
    }

    private void resolveUntilMayChoice() {
        while (gd.interaction.activeInteraction() == null && !gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
