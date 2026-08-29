package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TarriansSoulcleaver.class, GrizzlyBears.class, AngelsFeather.class, Forest.class})
class TarriansSoulcleaverTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has vigilance")
    void equippedCreatureHasVigilance() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        Permanent soulcleaver = addReady(player1, new TarriansSoulcleaver());
        soulcleaver.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Puts a +1/+1 counter on the equipped creature for another artifact or creature")
    void putsCounterForArtifactOrCreature() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        Permanent soulcleaver = addReady(player1, new TarriansSoulcleaver());
        soulcleaver.setAttachedTo(creature.getId());
        Permanent opponentCreature = addReady(player2, new GrizzlyBears());
        Permanent artifact = addReady(player1, new AngelsFeather());

        putIntoGraveyard(opponentCreature);
        putIntoGraveyard(artifact);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger for a land")
    void doesNotTriggerForLand() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        Permanent soulcleaver = addReady(player1, new TarriansSoulcleaver());
        soulcleaver.setAttachedTo(creature.getId());
        Permanent land = addReady(player2, new Forest());

        putIntoGraveyard(land);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Equip {2} attaches Tarrian's Soulcleaver to a creature you control")
    void equipAttachesSoulcleaver() {
        Permanent soulcleaver = addReady(player1, new TarriansSoulcleaver());
        Permanent creature = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(soulcleaver.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void putIntoGraveyard(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
    }
}
