package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.e.ExponentialGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WitherbloomPledgemage.class, BarkshellBlessing.class, GrizzlyBears.class,
        ExponentialGrowth.class})
class WitherbloomPledgemageTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant with Witherbloom Pledgemage gains 1 life")
    void castingInstantGainsLife() {
        addCreatureReady(player1, new WitherbloomPledgemage());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Copying an instant with Witherbloom Pledgemage gains 1 life for each trigger")
    void copyingInstantGainsLifeForEachTrigger() {
        addCreatureReady(player1, new WitherbloomPledgemage());
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, target.getId(),
                List.of(conspireA.getId(), conspireB.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    void castingSorceryGainsLifeBeforeTheSpellResolves() {
        Permanent pledgemage = addCreatureReady(player1, new WitherbloomPledgemage());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new ExponentialGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 1, pledgemage.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
    }

    @Test
    void opponentsInstantDoesNotTriggerMagecraft() {
        addCreatureReady(player1, new WitherbloomPledgemage());
        Permanent target = addCreatureReady(player2, new WitherbloomPledgemage());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new BarkshellBlessing()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(21);
    }

    @Test
    void castingCreatureDoesNotTriggerMagecraft() {
        addCreatureReady(player1, new WitherbloomPledgemage());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new WitherbloomPledgemage()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(countPermanents(player1, "Witherbloom Pledgemage")).isEqualTo(2);
    }

    @Test
    void eachPledgemageTriggersIndependently() {
        Permanent target = addCreatureReady(player1, new WitherbloomPledgemage());
        addCreatureReady(player1, new WitherbloomPledgemage());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new ExponentialGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 1, target.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }
}
