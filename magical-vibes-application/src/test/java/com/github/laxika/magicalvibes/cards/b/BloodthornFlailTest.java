package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodthornFlail.class, GrizzlyBears.class, Spellbook.class})
class BloodthornFlailTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsPlusTwoPlusOne() {
        Permanent creature = addCreatureReady(player1);
        Permanent flail = addFlailReady(player1);
        flail.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void equipCanBePaidWithThreeMana() {
        Permanent flail = addFlailReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(flail.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void equipCanBePaidByDiscardingACard() {
        Permanent flail = addFlailReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.setHand(player1, List.of(new Spellbook()));

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(flail.getAttachedTo()).isEqualTo(creature.getId());
        harness.assertInGraveyard(player1, "Spellbook");
    }

    private Permanent addFlailReady(Player player) {
        Permanent permanent = new Permanent(new BloodthornFlail());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addCreatureReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
