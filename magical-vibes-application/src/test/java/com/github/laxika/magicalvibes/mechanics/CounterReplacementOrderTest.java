package com.github.laxika.magicalvibes.mechanics;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.d.DoublingSeason;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HardenedScales;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A placement over several permanents that waits for its controller to order counter replacements (CR 616.1) resumes
 * the later permanents with their own amounts.
 */
@CardUsed({DoublingSeason.class, HardenedScales.class, GrizzlyBears.class, AirElemental.class})
class CounterReplacementOrderTest extends BaseCardTest {

    @Test
    @DisplayName("After an ordered placement, each later named permanent gets the amount evaluated for it")
    void laterNamedPermanentsKeepTheirOwnAmount() {
        harness.addToBattlefield(player1, new DoublingSeason());
        harness.addToBattlefield(player1, new HardenedScales());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Card spell = new Card();
        spell.setName("Growth Surge");
        spell.setType(CardType.SORCERY);
        List<CardEffect> effects = new ArrayList<>(List.of(new PutCounterOnTargetPermanentEffect(
                Subjects.creaturesYouControl(), CounterType.PLUS_ONE_PLUS_ONE, new SourcePower())));
        gd.stack.add(new StackEntry(StackEntryType.SORCERY_SPELL, spell, player1.getId(), "Growth Surge", effects));

        harness.passBothPriorities();
        chooseHardenedScalesFirst();
        chooseHardenedScalesFirst();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(10);
    }

    private void chooseHardenedScalesFirst() {
        PendingInteraction.ColorChoice order = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(order).isNotNull();
        String hardenedScales = order.options().stream()
                .filter(option -> option.contains("Hardened Scales"))
                .findFirst().orElseThrow();
        harness.handleListChoice(player1, hardenedScales);
    }
}
