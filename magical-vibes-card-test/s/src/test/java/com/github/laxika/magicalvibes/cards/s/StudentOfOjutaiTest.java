package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DragonFodder;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StudentOfOjutai.class, Shock.class, GrizzlyBears.class, DragonFodder.class})
class StudentOfOjutaiTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life when you cast a noncreature spell")
    void gainsLifeForNoncreatureSpell() {
        addCreatureReady(player1, new StudentOfOjutai());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Does not trigger when you cast a creature spell")
    void doesNotTriggerForCreatureSpell() {
        addCreatureReady(player1, new StudentOfOjutai());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLife(player1, 20);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each noncreature cast triggers, but creating creature tokens does not")
    void gainsLifeForEveryNoncreatureCast() {
        addCreatureReady(player1, new StudentOfOjutai());
        harness.setHand(player1, List.of(new DragonFodder(), new DragonFodder()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player1, 20);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(24);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each Student triggers independently for the same spell")
    void multipleStudentsEachGainLife() {
        addCreatureReady(player1, new StudentOfOjutai());
        addCreatureReady(player1, new StudentOfOjutai());
        harness.setHand(player1, List.of(new DragonFodder()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player1, 20);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(24);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger Student")
    void doesNotTriggerForOpponentSpell() {
        addCreatureReady(player1, new StudentOfOjutai());
        harness.setHand(player2, List.of(new DragonFodder()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castSorcery(player2, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }
}
