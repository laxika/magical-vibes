package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AdelizTheCinderWind;
import com.github.laxika.magicalvibes.cards.d.DanithaCapashenParagon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KethisTheHiddenHand;
import com.github.laxika.magicalvibes.cards.m.MoxAmber;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SisayWeatherlightCaptain.class, AdelizTheCinderWind.class,
        DanithaCapashenParagon.class, GrizzlyBears.class, KethisTheHiddenHand.class,
        MoxAmber.class, Shock.class})
class SisayWeatherlightCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each color among other legendary permanents you control")
    void getsPlusOneForEachColorAmongOtherLegendaryPermanents() {
        Permanent sisay = harness.addToBattlefieldAndReturn(player1, new SisayWeatherlightCaptain());

        assertThat(gqs.getEffectivePower(gd, sisay)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sisay)).isEqualTo(2);

        harness.addToBattlefield(player1, new AdelizTheCinderWind());
        harness.addToBattlefield(player1, new KethisTheHiddenHand());
        harness.addToBattlefield(player1, new DanithaCapashenParagon());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, sisay)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, sisay)).isEqualTo(7);
    }

    @Test
    @DisplayName("Searches for a legendary permanent with mana value less than Sisay's power")
    void searchesForLegendaryPermanentBelowPower() {
        harness.addToBattlefield(player1, new SisayWeatherlightCaptain());
        harness.addToBattlefield(player1, new DanithaCapashenParagon());

        MoxAmber moxAmber = new MoxAmber();
        DanithaCapashenParagon equalManaValue = new DanithaCapashenParagon();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(
                moxAmber, equalManaValue, new GrizzlyBears(), new Shock()));

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(moxAmber);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Mox Amber");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(moxAmber);
    }
}
