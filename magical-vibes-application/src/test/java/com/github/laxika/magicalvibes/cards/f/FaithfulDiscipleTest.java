package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AllThatGlitters;
import com.github.laxika.magicalvibes.cards.a.AngelicExaltation;
import com.github.laxika.magicalvibes.cards.a.AngelicGift;
import com.github.laxika.magicalvibes.cards.a.AnointedProcession;
import com.github.laxika.magicalvibes.cards.a.AuthorityOfTheConsuls;
import com.github.laxika.magicalvibes.cards.b.BanishingLight;
import com.github.laxika.magicalvibes.cards.c.CatharsCrusade;
import com.github.laxika.magicalvibes.cards.c.ClericClass;
import com.github.laxika.magicalvibes.cards.d.DivineVisitation;
import com.github.laxika.magicalvibes.cards.d.DuelistsHeritage;
import com.github.laxika.magicalvibes.cards.g.GauntletsOfLight;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.s.SigilOfTheEmptyThrone;
import com.github.laxika.magicalvibes.cards.s.SpectralSteel;
import com.github.laxika.magicalvibes.cards.t.TeleportationCircle;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FaithfulDisciple.class, Shock.class, AnointedProcession.class, CatharsCrusade.class,
        AuthorityOfTheConsuls.class, SigilOfTheEmptyThrone.class, AllThatGlitters.class,
        BanishingLight.class, DivineVisitation.class, DuelistsHeritage.class, GloriousAnthem.class,
        GauntletsOfLight.class, TeleportationCircle.class, AngelicGift.class, SpectralSteel.class,
        ClericClass.class, AngelicExaltation.class})
class FaithfulDiscipleTest extends BaseCardTest {

    @Test
    void whenFaithfulDiscipleDiesItOffersThreeSpellbookCardsAndPutsTheChoiceIntoHand() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new FaithfulDisciple());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, disciple.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).hasSize(3);

        var drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(drafted);
        harness.assertInGraveyard(player1, "Faithful Disciple");
    }
}
