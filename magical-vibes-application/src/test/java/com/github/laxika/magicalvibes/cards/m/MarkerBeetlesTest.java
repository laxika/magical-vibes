package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GoliathBeetle;
import com.github.laxika.magicalvibes.cards.p.PlatedSpider;
import com.github.laxika.magicalvibes.cards.y.YavimayaHollow;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarkerBeetles.class, GoliathBeetle.class, PlatedSpider.class, YavimayaHollow.class})
class MarkerBeetlesTest extends BaseCardTest {

    @Test
    @DisplayName("When Marker Beetles dies, target creature gets +1/+1 until end of turn")
    void deathTriggerBoostsTargetCreatureUntilEndOfTurn() {
        Permanent beetles = addCreatureReady(player1, new MarkerBeetles());
        Permanent target = addCreatureReady(player2, new PlatedSpider());
        Permanent blocker = addCreatureReady(player2, new GoliathBeetle());
        harness.addToBattlefield(player2, new YavimayaHollow());

        beetles.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("{2}, Sacrifice Marker Beetles: Draw a card")
    void sacrificeAbilityDrawsACard() {
        addCreatureReady(player1, new MarkerBeetles());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new PlatedSpider()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Marker Beetles");
        harness.assertInGraveyard(player1, "Marker Beetles");
        harness.assertInHand(player1, "Plated Spider");
    }
}
