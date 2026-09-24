package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SokenzanCrucibleOfDefiance.class, GrizzlyBears.class, HillGiant.class})
class SokenzanCrucibleOfDefianceTest extends BaseCardTest {

    @Test
    @DisplayName("Adds red mana")
    void addsRedMana() {
        harness.addToBattlefield(player1, new SokenzanCrucibleOfDefiance());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Channel creates two hasty Spirit tokens with legendary creature cost reductions")
    void channelCreatesHastySpiritTokensWithLegendaryCostReduction() {
        addLegendaryCreature(new GrizzlyBears());
        addLegendaryCreature(new HillGiant());

        harness.setHand(player1, List.of(new SokenzanCrucibleOfDefiance()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        List<Permanent> spirits = findPermanents(player1, "Spirit");
        assertThat(spirits).hasSize(2);
        assertThat(spirits).allMatch(permanent -> permanent.hasKeyword(Keyword.HASTE));
        harness.assertInGraveyard(player1, "Sokenzan, Crucible of Defiance");
    }

    @Test
    @DisplayName("Channel-granted haste expires at the end of the turn")
    void channelGrantedHasteExpiresAtEndOfTurn() {
        addLegendaryCreature(new GrizzlyBears());
        addLegendaryCreature(new HillGiant());

        harness.setHand(player1, List.of(new SokenzanCrucibleOfDefiance()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).allMatch(permanent -> permanent.hasKeyword(Keyword.HASTE));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).allMatch(permanent -> !permanent.hasKeyword(Keyword.HASTE));
    }

    private Permanent addLegendaryCreature(Card creature) {
        creature.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        return harness.addToBattlefieldAndReturn(player1, creature);
    }
}
