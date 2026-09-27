package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BlackCat;
import com.github.laxika.magicalvibes.cards.b.BloodhunterBat;
import com.github.laxika.magicalvibes.cards.c.CauldronFamiliar;
import com.github.laxika.magicalvibes.cards.c.CurseOfLeeches;
import com.github.laxika.magicalvibes.cards.c.CruelReality;
import com.github.laxika.magicalvibes.cards.e.ExpandedAnatomy;
import com.github.laxika.magicalvibes.cards.s.SorcerersBroom;
import com.github.laxika.magicalvibes.cards.t.TormentOfScarabs;
import com.github.laxika.magicalvibes.cards.t.TrespassersCurse;
import com.github.laxika.magicalvibes.cards.u.UnwillingIngredient;
import com.github.laxika.magicalvibes.cards.w.WitchsCauldron;
import com.github.laxika.magicalvibes.cards.w.WitchsCottage;
import com.github.laxika.magicalvibes.cards.w.WitchsFamiliar;
import com.github.laxika.magicalvibes.cards.w.WitchsOven;
import com.github.laxika.magicalvibes.cards.w.WitchsVengeance;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CurseboundWitch.class, WitchsCauldron.class, WitchsCottage.class,
        CauldronFamiliar.class, BloodhunterBat.class, CruelReality.class, WitchsVengeance.class,
        WitchsFamiliar.class, BlackCat.class, UnwillingIngredient.class, TormentOfScarabs.class,
        WitchsOven.class, CurseOfLeeches.class, SorcerersBroom.class, ExpandedAnatomy.class,
        TrespassersCurse.class, Shock.class})
class CurseboundWitchTest extends BaseCardTest {

    @Test
    void whenCurseboundWitchDiesItOffersThreeSpellbookCardsAndPutsTheChoiceIntoHand() {
        Permanent witch = harness.addToBattlefieldAndReturn(player1, new CurseboundWitch());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, witch.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).hasSize(3);

        var drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(drafted);
        harness.assertInGraveyard(player1, "Cursebound Witch");
    }
}
