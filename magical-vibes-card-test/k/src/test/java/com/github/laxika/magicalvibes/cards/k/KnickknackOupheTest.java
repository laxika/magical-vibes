package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AncestralMask;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KnickknackOuphe.class, HolyStrength.class, AncestralMask.class})
class KnickknackOupheTest extends BaseCardTest {

    @Test
    @DisplayName("enters with X counters and puts an eligible Aura onto the battlefield attached")
    void entersWithCountersAndPutsEligibleAuraOntoBattlefield() {
        KnickknackOuphe ouphe = new KnickknackOuphe();
        Card eligibleAura = new HolyStrength();
        Card tooExpensiveAura = new AncestralMask();
        harness.setHand(player1, List.of(ouphe));
        harness.setLibrary(player1, List.of(eligibleAura, tooExpensiveAura));
        harness.addMana(player1, ManaColor.GREEN, 3);

        gs.playCard(gd, player1, 0, 2, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(eligibleAura, tooExpensiveAura);
        assertThat(choice.validCardIds()).containsExactly(eligibleAura.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligibleAura.getId()));

        Permanent ouphePermanent = findPermanent(player1, "Knickknack Ouphe");
        Permanent auraPermanent = findPermanent(player1, "Holy Strength");
        assertThat(ouphePermanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(auraPermanent.getAttachedTo()).isEqualTo(ouphePermanent.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(tooExpensiveAura);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
