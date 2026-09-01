package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Mockingbird.class, GrizzlyBears.class, HillGiant.class})
class MockingbirdTest extends BaseCardTest {

    @Test
    @DisplayName("Uses total mana spent, restricts the copy choice, and keeps Bird and flying")
    void copiesCreatureWithinManaSpentLimit() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Mockingbird mockingbird = new Mockingbird();
        harness.setHand(player1, List.of(mockingbird));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        gs.playCard(gd, player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(bears.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(hillGiant.getId());

        harness.handlePermanentChosen(player1, bears.getId());

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getName().equals("Mockingbird"))
                .findFirst()
                .orElseThrow();
        assertThat(copy.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(copy.getCard().getSubtypes()).contains(CardSubtype.BIRD);
        assertThat(gqs.hasKeyword(gd, copy, Keyword.FLYING)).isTrue();
    }
}
