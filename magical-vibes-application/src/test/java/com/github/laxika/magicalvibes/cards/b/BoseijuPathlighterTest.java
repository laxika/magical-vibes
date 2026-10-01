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
}
