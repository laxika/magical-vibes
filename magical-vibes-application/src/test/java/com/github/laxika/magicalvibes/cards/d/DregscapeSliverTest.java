package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MuscleSliver;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DregscapeSliver.class, MuscleSliver.class, GrizzlyBears.class})
class DregscapeSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Dregscape Sliver can unearth itself")
    void canUnearthItself() {
        harness.setGraveyard(player1, List.of(new DregscapeSliver()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Dregscape Sliver");
        assertThat(returned.getGrantedKeywords()).contains(Keyword.HASTE);
        harness.assertNotInGraveyard(player1, "Dregscape Sliver");
    }

    @Test
    @DisplayName("Grants unearth {2} to Sliver creature cards in your graveyard")
    void grantsUnearthToSliverCreatureCard() {
        harness.addToBattlefield(player1, new DregscapeSliver());
        harness.setGraveyard(player1, List.of(new MuscleSliver()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Muscle Sliver");
        assertThat(returned.getGrantedKeywords()).contains(Keyword.HASTE);
        harness.assertNotInGraveyard(player1, "Muscle Sliver");
    }

    @Test
    @DisplayName("Does not grant unearth to non-Sliver creature cards")
    void doesNotGrantUnearthToNonSliverCreatureCard() {
        harness.addToBattlefield(player1, new DregscapeSliver());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An unearthed Dregscape Sliver is exiled at the next end step")
    void unearthedSliverIsExiledAtNextEndStep() {
        Card card = new DregscapeSliver();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dregscape Sliver");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(exiled -> exiled.getName().equals("Dregscape Sliver"));
    }
}
