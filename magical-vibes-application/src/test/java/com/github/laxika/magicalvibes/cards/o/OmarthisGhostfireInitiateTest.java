package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BurstOfStrength;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.PhyrexianWalker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OmarthisGhostfireInitiate.class, BurstOfStrength.class, PhyrexianWalker.class,
        GrizzlyBears.class, LightningBolt.class})
class OmarthisGhostfireInitiateTest extends BaseCardTest {

    @Test
    void entersWithXPlusOnePlusOneCounters() {
        harness.setHand(player1, List.of(new OmarthisGhostfireInitiate()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, 2);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Omarthis, Ghostfire Initiate")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void triggersOnlyForColorlessCreaturesAndMayPutACounterOnItself() {
        Permanent omarthis = addCreatureReady(player1, new OmarthisGhostfireInitiate());
        omarthis.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent colorlessCreature = addCreatureReady(player1, new PhyrexianWalker());
        Permanent coloredCreature = addCreatureReady(player1, new GrizzlyBears());

        putCounterOn(coloredCreature);
        resolveAllTriggers();
        assertThat(omarthis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        putCounterOn(colorlessCreature);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(omarthis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void diesAndManifestsOneCardForEachCounter() {
        Card first = new GrizzlyBears();
        Card second = new PhyrexianWalker();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third));

        Permanent omarthis = addCreatureReady(player1, new OmarthisGhostfireInitiate());
        omarthis.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, omarthis.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(Permanent::isManifested)
                .hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void putCounterOn(Permanent target) {
        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
