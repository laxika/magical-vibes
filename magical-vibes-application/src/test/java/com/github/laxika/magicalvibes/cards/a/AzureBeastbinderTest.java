package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AzureBeastbinder.class, AirElemental.class, FugitiveWizard.class, GrizzlyBears.class, HillGiant.class})
class AzureBeastbinderTest extends BaseCardTest {

    @Test
    @DisplayName("Can't be blocked by creatures with power 2 or greater")
    void cantBeBlockedByCreaturesWithPowerAtLeastTwo() {
        Permanent beastbinder = addCreatureReady(player1, new AzureBeastbinder());
        Permanent wizard = addCreatureReady(player2, new FugitiveWizard());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent hillGiant = addCreatureReady(player2, new HillGiant());

        assertThat(bls.canBlockAttacker(gd, wizard, beastbinder,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
        assertThat(bls.canBlockAttacker(gd, bears, beastbinder,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, hillGiant, beastbinder,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("Attacking makes an opposing creature lose abilities and become 2/2 until your next turn")
    void attackTriggerLastsUntilNextTurn() {
        Permanent beastbinder = addCreatureReady(player1, new AzureBeastbinder());
        Permanent elemental = addCreatureReady(player2, new AirElemental());

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds())
                .containsExactlyInAnyOrder(elemental.getId(), player1.getId())
                .doesNotContain(player2.getId());

        harness.handlePermanentChosen(player1, elemental.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(2);

        gs.declareBlockers(gd, player2, List.of());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(4);
    }
}
