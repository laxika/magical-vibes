package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KotisTheFangkeeper.class, Divination.class, GrizzlyBears.class})
class KotisTheFangkeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage exiles that many cards and only grants free casts up to that mana value")
    void exilesDamageAmountAndLimitsFreeCasts() {
        Permanent kotis = addAttackingKotis();
        Card eligible = new GrizzlyBears();
        Card tooExpensive = new Divination();
        harness.setLibrary(player2, List.of(eligible, tooExpensive));

        resolveCombatAndTrigger();

        assertThat(gd.getCardsExiledByPermanent(kotis.getId()))
                .extracting(Card::getId)
                .containsExactly(eligible.getId(), tooExpensive.getId());
        PendingInteraction.ImprovisationCapstoneCastChoice interaction =
                (PendingInteraction.ImprovisationCapstoneCastChoice) gd.interaction.activeInteraction();
        assertThat(interaction.validCardIds()).contains(eligible.getId());
        assertThat(interaction.validCardIds()).doesNotContain(tooExpensive.getId());
    }

    @Test
    @DisplayName("Any number of eligible exiled spells can be cast without paying their mana costs")
    void castsAnyNumberOfEligibleExiledSpells() {
        addAttackingKotis();
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setLibrary(player2, List.of(first, second));

        resolveCombatAndTrigger();

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(first.getId())).isNull();
        assertThat(gd.findExiledCard(second.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(first.getId(), second.getId());
    }

    private Permanent addAttackingKotis() {
        Permanent kotis = addCreatureReady(player1, new KotisTheFangkeeper());
        kotis.setAttacking(true);
        return kotis;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
