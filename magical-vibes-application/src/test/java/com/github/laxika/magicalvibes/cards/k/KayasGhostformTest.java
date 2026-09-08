package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GideonBlackblade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VraskasContempt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KayasGhostform.class, GrizzlyBears.class, GideonBlackblade.class, DoomBlade.class,
        Disperse.class, VraskasContempt.class})
class KayasGhostformTest extends BaseCardTest {

    @Test
    void returnsEnchantedCreatureAfterItDies() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castGhostform(creature);

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getCard().getId()));
    }

    @Test
    void returnsEnchantedPermanentAfterItIsExiled() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castGhostform(creature);

        harness.setHand(player1, List.of(new VraskasContempt()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getCard().getId()));
    }

    @Test
    void canEnchantAndReturnAPlaneswalker() {
        GideonBlackblade card = new GideonBlackblade();
        Permanent planeswalker = new Permanent(card);
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        gd.playerBattlefields.get(player1.getId()).add(planeswalker);
        castGhostform(planeswalker);

        harness.setHand(player1, List.of(new VraskasContempt()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castInstant(player1, 0, planeswalker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(returned -> returned.getCard().getId().equals(card.getId())
                        && returned.getCounterCount(CounterType.LOYALTY) == 4);
    }

    @Test
    void doesNotReturnAnEnchantedPermanentBouncedToHand() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castGhostform(creature);

        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(creature.getCard().getId()));
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void cannotEnchantAnOpponentPermanent() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KayasGhostform()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castGhostform(Permanent target) {
        harness.setHand(player1, List.of(new KayasGhostform()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
