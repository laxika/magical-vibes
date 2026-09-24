package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EmpressGalina;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TsaboTavoc;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SisayWeatherlightCaptain.class, EmpressGalina.class, TsaboTavoc.class, GrizzlyBears.class})
class SisayWeatherlightCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each color among other legendary permanents you control")
    void boostsForColorsAmongOtherControlledLegends() {
        Permanent sisay = harness.addToBattlefieldAndReturn(player1, new SisayWeatherlightCaptain());
        harness.addToBattlefield(player1, new EmpressGalina());
        harness.addToBattlefield(player1, new TsaboTavoc());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new TsaboTavoc());

        assertThat(gqs.getEffectivePower(gd, sisay)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, sisay)).isEqualTo(5);
    }

    @Test
    @DisplayName("Searches for a legendary permanent with mana value less than Sisay's power")
    void searchesForEligibleLegendaryPermanent() {
        harness.addToBattlefield(player1, new SisayWeatherlightCaptain());
        Card eligible = legendaryPermanent("Eligible", "{1}");
        Card equalToPower = legendaryPermanent("Equal to power", "{2}");
        Card nonlegendary = permanent("Nonlegendary", "{1}");
        harness.setLibrary(player1, List.of(eligible, equalToPower, nonlegendary));
        addFiveColorMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(eligible);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Eligible");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(equalToPower, nonlegendary);
    }

    private void addFiveColorMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private Card legendaryPermanent(String name, String manaCost) {
        Card card = permanent(name, manaCost);
        card.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        return card;
    }

    private Card permanent(String name, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setManaCost(manaCost);
        return card;
    }
}
