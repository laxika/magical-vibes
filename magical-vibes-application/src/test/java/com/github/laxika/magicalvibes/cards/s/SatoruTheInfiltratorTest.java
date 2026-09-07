package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RaiseTheAlarm;
import com.github.laxika.magicalvibes.cards.z.Zombify;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SatoruTheInfiltrator.class, GrizzlyBears.class, Zombify.class, RaiseTheAlarm.class})
class SatoruTheInfiltratorTest extends BaseCardTest {

    @Test
    void drawsWhenANontokenCreatureWasNotCast() {
        harness.addToBattlefield(player1, new SatoruTheInfiltrator());
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotDrawWhenCreatureWasCastWithMana() {
        harness.addToBattlefield(player1, new SatoruTheInfiltrator());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void drawsWhenSatoruEntersWithoutBeingCast() {
        SatoruTheInfiltrator satoru = new SatoruTheInfiltrator();
        harness.setGraveyard(player1, List.of(satoru));
        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, satoru.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotDrawWhenTokensEnter() {
        harness.addToBattlefield(player1, new SatoruTheInfiltrator());
        harness.setHand(player1, List.of(new RaiseTheAlarm()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
    }
}
