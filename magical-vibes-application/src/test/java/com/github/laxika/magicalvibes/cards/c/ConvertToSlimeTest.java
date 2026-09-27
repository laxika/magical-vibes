package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeartbeatOfSpring;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        ConvertToSlime.class,
        FountainOfYouth.class,
        Forest.class,
        GrizzlyBears.class,
        HeartbeatOfSpring.class,
        Shock.class
})
class ConvertToSlimeTest extends BaseCardTest {

    @Test
    void destroysEachTypeAndCreatesOozeWithTotalManaValueWhenDeliriumIsActive() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new HeartbeatOfSpring());
        harness.setGraveyard(player1, List.of(new Forest(), new Shock()));
        harness.setHand(player1, List.of(new ConvertToSlime()));
        addManaForConvertToSlime();

        harness.castSorcery(player1, 0, List.of(artifact.getId(), creature.getId(), enchantment.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Heartbeat of Spring");
        Permanent ooze = findPermanent(player1, "Ooze");
        assertThat(ooze.getCard().getPower()).isEqualTo(5);
        assertThat(ooze.getCard().getToughness()).isEqualTo(5);
    }

    @Test
    void doesNotCreateOozeWithoutDelirium() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new ConvertToSlime()));
        addManaForConvertToSlime();

        harness.castSorcery(player1, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Ooze")).isEmpty();
    }

    @Test
    void rejectsMoreThanOneTargetOfTheSameType() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ConvertToSlime()));
        addManaForConvertToSlime();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(firstCreature.getId(), secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at most one creature");
    }

    private void addManaForConvertToSlime() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
