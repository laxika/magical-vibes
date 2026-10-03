package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.EmergenceZone;
import com.github.laxika.magicalvibes.cards.f.FieldOfRuin;
import com.github.laxika.magicalvibes.cards.g.GingerbreadCabin;
import com.github.laxika.magicalvibes.cards.h.HallOfOracles;
import com.github.laxika.magicalvibes.cards.k.KhalniGarden;
import com.github.laxika.magicalvibes.cards.m.MemorialToUnity;
import com.github.laxika.magicalvibes.cards.m.MobilizedDistrict;
import com.github.laxika.magicalvibes.cards.r.RadiantFountain;
import com.github.laxika.magicalvibes.cards.r.RoadsideReliquary;
import com.github.laxika.magicalvibes.cards.s.ScavengerGrounds;
import com.github.laxika.magicalvibes.cards.s.SecludedCourtyard;
import com.github.laxika.magicalvibes.cards.t.TreasureVault;
import com.github.laxika.magicalvibes.cards.t.ThrivingGrove;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoseijuPathlighter.class, BondersEnclave.class, BoseijuWhoEndures.class,
        EmergenceZone.class, FieldOfRuin.class, GingerbreadCabin.class, HallOfOracles.class,
        KhalniGarden.class, MemorialToUnity.class, MobilizedDistrict.class, RadiantFountain.class,
        RoadsideReliquary.class, ScavengerGrounds.class, SecludedCourtyard.class,
        ThrivingGrove.class, TreasureVault.class})
class BoseijuPathlighterTest extends BaseCardTest {

    @Test
    void entersAndOffersThreeSpellbookChoices() {
        harness.enterBattlefieldAndReturn(player1, new BoseijuPathlighter());
        resolveAllTriggers();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).hasSize(3);

        Card drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(drafted);
    }

    @Test
    void castingCreatesOnlyTheChosenSpellbookCardInHand() {
        harness.castFromHand(player1, new BoseijuPathlighter(), "{2}{G}");
        resolveAllTriggers();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.cards()).hasSize(3);
        assertThat(choice.cards().stream().map(Card::getName)).doesNotHaveDuplicates()
                .allMatch(List.of("Field of Ruin", "Bonders' Enclave", "Radiant Fountain",
                        "Thriving Grove", "Treasure Vault", "Gingerbread Cabin", "Memorial to Unity",
                        "Boseiju, Who Endures", "Secluded Courtyard", "Roadside Reliquary",
                        "Scavenger Grounds", "Emergence Zone", "Khalni Garden",
                        "Mobilized District", "Hall of Oracles")::contains);
        Card drafted = choice.cards().getLast();
        int librarySize = gd.playerDecks.get(player1.getId()).size();

        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drafted);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySize);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard()).noneMatch(card -> card == drafted);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentReceivesTheirOwnDraftedCard() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.enterBattlefieldAndReturn(player2, new BoseijuPathlighter());
        resolveAllTriggers();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        Card drafted = choice.cards().getFirst();

        harness.handleMultipleCardsChosen(player2, List.of(drafted.getId()));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drafted);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void triggerStillDraftsAfterSourceLeavesBattlefield() {
        var permanent = harness.enterBattlefieldAndReturn(player1, new BoseijuPathlighter());
        gd.playerBattlefields.get(player1.getId()).remove(permanent);
        gd.playerGraveyards.get(player1.getId()).add(permanent.getCard());
        resolveAllTriggers();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        Card drafted = choice.cards().getFirst();

        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(drafted);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(permanent.getCard());
    }
}
