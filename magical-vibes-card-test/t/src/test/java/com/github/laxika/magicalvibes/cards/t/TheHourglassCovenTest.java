package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.o.Owlbear;
import com.github.laxika.magicalvibes.cards.w.WyllPactBoundDuelist;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheHourglassCoven.class, WyllPactBoundDuelist.class, Owlbear.class})
class TheHourglassCovenTest extends BaseCardTest {

    @Test
    void draftsTwiceBeforePuttingEitherHagOntoTheBattlefield() {
        harness.enterBattlefieldAndReturn(player1, new TheHourglassCoven());
        harness.passBothPriorities();

        PendingInteraction.SpellbookDraftChoice firstDraft = gd.interaction
                .activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(firstDraft).isNotNull();
        assertThat(firstDraft.playerId()).isEqualTo(player1.getId());
        assertThat(firstDraft.cards()).hasSize(3);
        assertThat(firstDraft.cards()).extracting(card -> card.getName()).doesNotHaveDuplicates();
        harness.handleMultipleCardsChosen(player1, List.of(firstDraft.cards().getFirst().getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        PendingInteraction.SpellbookDraftChoice secondDraft = gd.interaction
                .activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(secondDraft).isNotNull();
        assertThat(secondDraft.cards()).hasSize(3);
        harness.handleMultipleCardsChosen(player1, List.of(secondDraft.cards().getFirst().getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void doesNotBuffItself() {
        TheHourglassCoven card = new TheHourglassCoven();
        int basePower = card.getPower();
        int baseToughness = card.getToughness();
        Permanent coven = harness.addToBattlefieldAndReturn(player1, card);

        assertThat(gqs.getEffectivePower(gd, coven)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, coven)).isEqualTo(baseToughness);
    }

    @Test
    void buffsOtherWarlocksYouControlOnly() {
        Permanent ownWarlock = harness.addToBattlefieldAndReturn(player1, new WyllPactBoundDuelist());
        Permanent ownNonWarlock = harness.addToBattlefieldAndReturn(player1, new Owlbear());
        Permanent opposingWarlock = harness.addToBattlefieldAndReturn(player2, new WyllPactBoundDuelist());

        int ownWarlockPower = gqs.getEffectivePower(gd, ownWarlock);
        int ownWarlockToughness = gqs.getEffectiveToughness(gd, ownWarlock);
        int ownNonWarlockPower = gqs.getEffectivePower(gd, ownNonWarlock);
        int ownNonWarlockToughness = gqs.getEffectiveToughness(gd, ownNonWarlock);
        int opposingWarlockPower = gqs.getEffectivePower(gd, opposingWarlock);
        int opposingWarlockToughness = gqs.getEffectiveToughness(gd, opposingWarlock);

        harness.addToBattlefield(player1, new TheHourglassCoven());

        assertThat(gqs.getEffectivePower(gd, ownWarlock)).isEqualTo(ownWarlockPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, ownWarlock)).isEqualTo(ownWarlockToughness + 1);
        assertThat(gqs.getEffectivePower(gd, ownNonWarlock)).isEqualTo(ownNonWarlockPower);
        assertThat(gqs.getEffectiveToughness(gd, ownNonWarlock)).isEqualTo(ownNonWarlockToughness);
        assertThat(gqs.getEffectivePower(gd, opposingWarlock)).isEqualTo(opposingWarlockPower);
        assertThat(gqs.getEffectiveToughness(gd, opposingWarlock)).isEqualTo(opposingWarlockToughness);
    }
}
