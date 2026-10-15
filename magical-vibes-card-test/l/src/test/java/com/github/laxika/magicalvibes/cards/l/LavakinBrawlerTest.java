package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FireElemental;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LavakinBrawler.class, FireElemental.class, GreenwoodSentinel.class, Unsummon.class})
class LavakinBrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts the Elemental-count trigger on the stack")
    void attackPutsTriggerOnStack() {
        addCreatureReady(player1, new LavakinBrawler());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("Gets +1/+0 for each Elemental controlled")
    void boostScalesWithControlledElementals() {
        Permanent brawler = addCreatureReady(player1, new LavakinBrawler());
        addCreatureReady(player1, new FireElemental());
        addCreatureReady(player1, new GreenwoodSentinel());
        addCreatureReady(player2, new FireElemental());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(brawler.getPowerModifier()).isEqualTo(2);
        assertThat(brawler.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent brawler = addCreatureReady(player1, new LavakinBrawler());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(brawler.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(brawler.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Counts Elementals that enter before the attack trigger resolves")
    void countsElementalsAtResolution() {
        Permanent brawler = addCreatureReady(player1, new LavakinBrawler());

        declareAttackers(player1, List.of(0));
        addCreatureReady(player1, new FireElemental());
        resolveAllTriggers();

        assertThat(brawler.getPowerModifier()).isEqualTo(2);
        assertThat(brawler.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Elementals leaving before resolution do not count")
    void excludesElementalsThatLeaveBeforeResolution() {
        Permanent brawler = addCreatureReady(player1, new LavakinBrawler());
        Permanent elemental = addCreatureReady(player1, new FireElemental());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackers(player1, List.of(0));
        harness.castAndResolveInstant(player1, 0, elemental.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Fire Elemental");
        assertThat(brawler.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Elementals entering after resolution do not change the boost")
    void boostIsFixedAfterResolution() {
        Permanent brawler = addCreatureReady(player1, new LavakinBrawler());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        addCreatureReady(player1, new FireElemental());

        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(3);
        assertThat(brawler.getPowerModifier()).isEqualTo(1);
    }
}
