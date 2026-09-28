package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeceptiveFrostkite.class, ColossalDreadmaw.class, GrizzlyBears.class})
class DeceptiveFrostkiteTest extends BaseCardTest {

    @Test
    void copiesOnlyAControlledCreatureWithPowerFourOrGreaterAndAddsExceptions() {
        Permanent eligible = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());
        Permanent tooSmall = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentEligible = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        DeceptiveFrostkite frostkite = new DeceptiveFrostkite();

        harness.castFromHand(player1, frostkite, "{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(eligible.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(tooSmall.getId(), opponentEligible.getId());

        harness.handlePermanentChosen(player1, eligible.getId());

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(frostkite.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.effectiveCreatureSubtypes(gd, copy)).contains(CardSubtype.DRAGON);
        assertThat(gqs.hasKeyword(gd, copy, Keyword.FLYING)).isTrue();
    }
}
