package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.ChildOfAlara;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({JensonCarthalionDruidExile.class, ChildOfAlara.class, GrizzlyBears.class})
class JensonCarthalionDruidExileTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an all-color spell scries 1 and creates a 4/4 Angel")
    void allColorSpellScriesAndCreatesAngel() {
        harness.addToBattlefield(player1, new JensonCarthalionDruidExile());
        List<Card> library = List.of(new GrizzlyBears());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new ChildOfAlara()));
        addFiveColors();

        harness.castCreature(player1, 0);
        resolveTriggerAndScry();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        Permanent angel = findPermanent(player1, "Angel");
        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("The mana ability adds one mana of each color")
    void manaAbilityAddsAllFiveColors() {
        Permanent jenson = harness.addToBattlefieldAndReturn(player1, new JensonCarthalionDruidExile());
        jenson.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(jenson.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    private void addFiveColors() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private void resolveTriggerAndScry() {
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();
    }
}
