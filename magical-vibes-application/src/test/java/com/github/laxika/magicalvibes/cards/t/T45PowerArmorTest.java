package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({T45PowerArmor.class, GrizzlyBears.class})
class T45PowerArmorTest extends BaseCardTest {

    @Test
    void entersWithTwoEnergyCounters() {
        harness.setHand(player1, List.of(new T45PowerArmor()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void boostsAndLocksEquippedCreature() {
        Permanent armor = addArmorReady(player1);
        Permanent creature = addCreatureReady(player1);
        armor.setAttachedTo(creature.getId());
        creature.tap();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);

        advanceToUpkeep(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void paysEnergyToUntapAndPutChosenKeywordCounterOnEquippedCreature() {
        Permanent armor = addArmorReady(player1);
        Permanent creature = addCreatureReady(player1);
        armor.setAttachedTo(creature.getId());
        creature.tap();
        gd.playerEnergyCounters.put(player1.getId(), 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Put a trample counter on equipped creature");

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getCounterCount(CounterType.TRAMPLE)).isEqualTo(1);
    }

    private Permanent addArmorReady(Player player) {
        Permanent permanent = new Permanent(new T45PowerArmor());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }
}
