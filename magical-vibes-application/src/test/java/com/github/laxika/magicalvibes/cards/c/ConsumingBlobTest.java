package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConsumingBlob.class, GrizzlyBears.class, GloriousAnthem.class, Millstone.class,
        MindRot.class, Opt.class, Ornithopter.class, Plains.class})
class ConsumingBlobTest extends BaseCardTest {

    @Test
    @DisplayName("Power is the number of distinct card types in your graveyard and toughness is one greater")
    void powerAndToughnessCountDistinctCardTypes() {
        Permanent blob = addBlobReady(player1);
        harness.setGraveyard(player1, List.of(
                new Plains(), new Opt(), new MindRot(), new GrizzlyBears(), new Millstone(), new GloriousAnthem()
        ));

        assertThat(gqs.getEffectivePower(gd, blob)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, blob)).isEqualTo(7);
    }

    @Test
    @DisplayName("A card with multiple types contributes each type, and the opponent's graveyard is ignored")
    void countsEachTypeAndOnlyOwnGraveyard() {
        Permanent blob = addBlobReady(player1);
        harness.setGraveyard(player1, List.of(new Plains(), new Ornithopter()));
        harness.setGraveyard(player2, List.of(new Opt(), new MindRot(), new GloriousAnthem()));

        assertThat(gqs.getEffectivePower(gd, blob)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, blob)).isEqualTo(4);
    }

    @Test
    @DisplayName("At your end step, creates an Ooze token with the same dynamic power and toughness")
    void createsDynamicOozeTokenAtEndStep() {
        addBlobReady(player1);
        harness.setGraveyard(player1, List.of(new Plains(), new Opt()));

        advanceToEndStep(player1);

        Permanent ooze = findPermanents(player1, "Ooze").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, ooze)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ooze)).isEqualTo(3);

        gd.playerGraveyards.get(player1.getId()).add(new MindRot());
        assertThat(gqs.getEffectivePower(gd, ooze)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ooze)).isEqualTo(4);
    }

    private Permanent addBlobReady(Player player) {
        return addCreatureReady(player, new ConsumingBlob());
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
