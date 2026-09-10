package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AkoumBattlesinger;
import com.github.laxika.magicalvibes.cards.h.HadaFreeblade;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GeneralTazri.class, HadaFreeblade.class, GrizzlyBears.class, AkoumBattlesinger.class})
class GeneralTazriTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the enter-the-battlefield ability searches for an Ally")
    void acceptsAllySearch() {
        harness.setLibrary(player1, List.of(new HadaFreeblade(), new GrizzlyBears()));
        castGeneralTazri();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).extracting(Card::getName).containsExactly("Hada Freeblade");

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Hada Freeblade");
    }

    @Test
    @DisplayName("Declining the enter-the-battlefield ability does not search")
    void declinesAllySearch() {
        Card ally = new HadaFreeblade();
        harness.setLibrary(player1, List.of(ally));
        castGeneralTazri();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ally);
    }

    @Test
    @DisplayName("The activated ability boosts Allies by the number of their distinct colors")
    void boostsAlliesByDistinctColors() {
        Permanent general = addCreatureReady(player1, new GeneralTazri());
        Permanent whiteAlly = addCreatureReady(player1, new HadaFreeblade());
        Permanent redAlly = addCreatureReady(player1, new AkoumBattlesinger());
        Permanent nonAlly = addCreatureReady(player1, new GrizzlyBears());

        int generalPower = gqs.getEffectivePower(gd, general);
        int generalToughness = gqs.getEffectiveToughness(gd, general);
        int whiteAllyPower = gqs.getEffectivePower(gd, whiteAlly);
        int whiteAllyToughness = gqs.getEffectiveToughness(gd, whiteAlly);
        int redAllyPower = gqs.getEffectivePower(gd, redAlly);
        int redAllyToughness = gqs.getEffectiveToughness(gd, redAlly);
        int nonAllyPower = gqs.getEffectivePower(gd, nonAlly);
        int nonAllyToughness = gqs.getEffectiveToughness(gd, nonAlly);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, general)).isEqualTo(generalPower + 2);
        assertThat(gqs.getEffectiveToughness(gd, general)).isEqualTo(generalToughness + 2);
        assertThat(gqs.getEffectivePower(gd, whiteAlly)).isEqualTo(whiteAllyPower + 2);
        assertThat(gqs.getEffectiveToughness(gd, whiteAlly)).isEqualTo(whiteAllyToughness + 2);
        assertThat(gqs.getEffectivePower(gd, redAlly)).isEqualTo(redAllyPower + 2);
        assertThat(gqs.getEffectiveToughness(gd, redAlly)).isEqualTo(redAllyToughness + 2);
        assertThat(gqs.getEffectivePower(gd, nonAlly)).isEqualTo(nonAllyPower);
        assertThat(gqs.getEffectiveToughness(gd, nonAlly)).isEqualTo(nonAllyToughness);
    }

    private void castGeneralTazri() {
        harness.setHand(player1, List.of(new GeneralTazri()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
