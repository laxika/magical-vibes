package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LurkingSpinecrawler.class, GrizzlyBears.class, Opt.class, Watchwolf.class})
class LurkingSpinecrawlerTest extends BaseCardTest {

    @Test
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
        assertThat(gd.perpetualTriggeredAbilityGrants.get(chosen.getId()))
                .containsKey(EffectSlot.ON_SELF_CAST);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
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
}
