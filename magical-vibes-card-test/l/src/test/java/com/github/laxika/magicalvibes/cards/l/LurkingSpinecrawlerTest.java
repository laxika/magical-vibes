package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LurkingSpinecrawler.class, GrizzlyBears.class, Opt.class, Watchwolf.class, Forest.class})
class LurkingSpinecrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("The chosen card incorporates {1}{B} and its cast trigger makes an opponent sacrifice a creature")
    void incorporatesNonlandCardAndItsCastTriggerMakesAnOpponentSacrifice() {
        LurkingSpinecrawler crawler = new LurkingSpinecrawler();
        Opt chosen = new Opt();
        harness.setHand(player1, List.of(crawler, chosen));
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.PerpetualPowerToughnessChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PerpetualPowerToughnessChoice.class);
        assertThat(choice.validIndices()).containsExactly(0);
        harness.handleCardChosen(player1, 0);

        ManaCost incorporation = gd.perpetualManaCostIncreases.get(chosen.getId());
        assertThat(incorporation.countColorSymbols(ManaColor.BLACK)).isEqualTo(1);
        assertThat(incorporation.getGenericCost()).isEqualTo(1);
        assertThat(gd.perpetualTriggerEffects(chosen.getId(), Triggers.youCastThisSpell(), Zone.STACK)).isNotEmpty();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A multicolored spell drains each opponent once for each of its colors")
    void multicoloredSpellDrainsEachOpponentForEachColor() {
        harness.addToBattlefield(player1, new LurkingSpinecrawler());
        harness.setHand(player1, List.of(new Watchwolf()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("An opponent who can sacrifice a creature must, and discards nothing (Oracle: no \"unless\")")
    void opponentWithACreatureSacrificesAndKeepsTheirHand() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Forest()));

        castIncorporatedOpt();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent who can't sacrifice a creature discards a card")
    void opponentWithoutACreatureDiscards() {
        harness.setHand(player2, List.of(new Forest()));

        castIncorporatedOpt();

        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    private void castIncorporatedOpt() {
        harness.setHand(player1, List.of(new LurkingSpinecrawler(), new Opt()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class) != null) {
            harness.handleCardChosen(player2, 0);
        }
    }
}
