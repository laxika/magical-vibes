package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScoutForSurvivors.class, EliteVanguard.class, LanternKami.class, SavannahLions.class, HillGiant.class})
class ScoutForSurvivorsTest extends BaseCardTest {

    @Test
    void returnsUpToThreeCreaturesWithinManaValueLimitWithCounters() {
        Card first = new EliteVanguard();
        Card second = new LanternKami();
        Card third = new SavannahLions();
        Card spell = new ScoutForSurvivors();
        harness.setGraveyard(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(3);
        assertThat(choice.maxTotalManaValue()).isEqualTo(3);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(first.getId(), second.getId(), third.getId());

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        for (Card card : List.of(first, second, third)) {
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(permanent -> permanent.getCard().getId().equals(card.getId())
                            && permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 1);
        }
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(spell.getId());
    }

    @Test
    void rejectsTargetsWhoseTotalManaValueExceedsThree() {
        Card cheap = new EliteVanguard();
        Card expensive = new HillGiant();
        harness.setGraveyard(player1, List.of(cheap, expensive));
        harness.setHand(player1, List.of(new ScoutForSurvivors()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(cheap.getId(), expensive.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
    }
}
