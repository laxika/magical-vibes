package com.github.laxika.magicalvibes.cards.r;

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

@CardUsed({RakdosRiteknife.class, GrizzlyBears.class})
class RakdosRiteknifeTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsPowerForEachBloodCounter() {
        Permanent creature = addReadyCreature(player1);
        Permanent riteknife = addReadyRiteknife();
        riteknife.setAttachedTo(creature.getId());
        riteknife.setCounterCount(CounterType.BLOOD, 2);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void tappingAndSacrificingCreatureAddsBloodCounter() {
        Permanent riteknife = addReadyRiteknife();
        Permanent creature = addReadyCreature(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(riteknife.isTapped()).isTrue();
        assertThat(riteknife.getCounterCount(CounterType.BLOOD)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    void sacrificingRiteknifeMakesTargetPlayerSacrificeForEachBloodCounter() {
        Permanent riteknife = addReadyRiteknife();
        riteknife.setCounterCount(CounterType.BLOOD, 2);
        addReadyCreature(player2);
        addReadyCreature(player2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(riteknife);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void targetPlayerChoosesWhichPermanentsToSacrifice() {
        Permanent riteknife = addReadyRiteknife();
        riteknife.setCounterCount(CounterType.BLOOD, 1);
        addReadyCreature(player2);
        addReadyCreature(player2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultiplePermanentsChosen(player2,
                List.of(gd.playerBattlefields.get(player2.getId()).getFirst().getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    private Permanent addReadyRiteknife() {
        Permanent riteknife = new Permanent(new RakdosRiteknife());
        riteknife.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(riteknife);
        return riteknife;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }
}
