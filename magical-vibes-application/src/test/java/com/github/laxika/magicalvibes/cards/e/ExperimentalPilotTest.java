package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BomatBazaarBarge;
import com.github.laxika.magicalvibes.cards.d.DemolitionStomper;
import com.github.laxika.magicalvibes.cards.f.FuturistSentinel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MechtitanCore;
import com.github.laxika.magicalvibes.cards.r.RaidersKarve;
import com.github.laxika.magicalvibes.cards.r.ReckonerBankbuster;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExperimentalPilot.class, BomatBazaarBarge.class, RaidersKarve.class,
        DemolitionStomper.class, FuturistSentinel.class, MechtitanCore.class,
        ReckonerBankbuster.class, GrizzlyBears.class})
class ExperimentalPilotTest extends BaseCardTest {

    @Test
    void paysManaAndDiscardsTwoCardsToDraftFromItsSpellbook() {
        harness.addToBattlefield(player1, new ExperimentalPilot());
        Card firstDiscard = new GrizzlyBears();
        Card secondDiscard = new GrizzlyBears();
        harness.setHand(player1, List.of(firstDiscard, secondDiscard));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firstDiscard, secondDiscard);

        Card drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drafted);
    }

    @Test
    void crewsAVehicleAsThoughItsPowerWereTwoGreater() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new FuturistSentinel());
        sentinel.setSummoningSick(false);
        Permanent pilot = harness.addToBattlefieldAndReturn(player1, new ExperimentalPilot());
        pilot.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, sentinel)).isTrue();
        assertThat(pilot.isTapped()).isTrue();
    }
}
