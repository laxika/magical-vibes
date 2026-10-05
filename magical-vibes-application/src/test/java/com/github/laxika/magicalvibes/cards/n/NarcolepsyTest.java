package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Narcolepsy.class, GlorySeeker.class})
class NarcolepsyTest extends BaseCardTest {

    @Test
    @DisplayName("Taps the untapped enchanted creature during upkeep")
    void tapsUntappedEnchantedCreatureDuringUpkeep() {
        Permanent bears = addCreatureReady(player1, new GlorySeeker());
        addAttachedAura(bears);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not trigger for a tapped enchanted creature")
    void doesNotTriggerForTappedEnchantedCreature() {
        Permanent bears = addCreatureReady(player1, new GlorySeeker());
        bears.tap();
        addAttachedAura(bears);
        advanceToUpkeepWithoutUntapping(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Triggers during the enchanted creature controller's upkeep")
    void triggersDuringEnchantedCreatureControllersUpkeep() {
        Permanent bears = addCreatureReady(player2, new GlorySeeker());
        addAttachedAura(bears);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    void castingOnOpponentsCreatureDoesNotTapItUntilUpkeep() {
        Permanent creature = addCreatureReady(player2, new GlorySeeker());
        Permanent other = addCreatureReady(player2, new GlorySeeker());
        harness.setHand(player1, List.of(new Narcolepsy()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Narcolepsy").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(creature.isTapped()).isFalse();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(other.isTapped()).isFalse();
    }

    @Test
    void allowsNormalUntapBeforeTappingAgainInUpkeep() {
        Permanent creature = addCreatureReady(player1, new GlorySeeker());
        creature.tap();
        addAttachedAura(creature);

        advanceToUpkeep(player1);

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void untappingAfterUpkeepBeginsDoesNotCreateATrigger() {
        Permanent creature = addCreatureReady(player1, new GlorySeeker());
        creature.tap();
        addAttachedAura(creature);

        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();
        creature.untap();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void removingAuraInResponseDoesNotStopItsTrigger() {
        Permanent creature = addCreatureReady(player1, new GlorySeeker());
        Permanent aura = addAttachedAura(creature);
        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    private Permanent addAttachedAura(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Narcolepsy());
        aura.setAttachedTo(creature.getId());
        return aura;
    }

    private void advanceToUpkeepWithoutUntapping(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(player, TurnStep.UPKEEP);
    }
}
