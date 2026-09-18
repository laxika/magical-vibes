package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FeedbackBolt.class, ChandraNalaar.class, Ornithopter.class, RagingGoblin.class})
class FeedbackBoltTest extends BaseCardTest {

    private void cast() {
        cast(player2.getId());
    }

    private void cast(UUID targetId) {
        prepareCast();
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new FeedbackBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("Deals damage equal to the number of artifacts you control")
    void dealsDamageEqualToArtifactCount() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());

        int before = gd.getLife(player2.getId());
        cast();

        assertThat(gd.getLife(player2.getId())).isEqualTo(before - 2);
    }

    @Test
    @DisplayName("Counts only artifacts controlled by the caster")
    void countsOnlyArtifactsControlledByCaster() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());

        int before = gd.getLife(player2.getId());
        cast();

        assertThat(gd.getLife(player2.getId())).isEqualTo(before - 1);
    }

    @Test
    @DisplayName("Deals no damage when you control no artifacts")
    void dealsNoDamageWithoutArtifacts() {
        harness.addToBattlefield(player1, new RagingGoblin());

        int before = gd.getLife(player2.getId());
        cast();

        assertThat(gd.getLife(player2.getId())).isEqualTo(before);
    }

    @Test
    @DisplayName("Counts artifacts when it resolves")
    void countsArtifactsAtResolution() {
        prepareCast();
        harness.castInstant(player1, 0, player2.getId());
        harness.addToBattlefield(player1, new Ornithopter());

        int before = gd.getLife(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(before - 1);
    }

    @Test
    @DisplayName("Deals damage to a target planeswalker")
    void dealsDamageToPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        target.setCounterCount(CounterType.LOYALTY, 4);
        harness.addToBattlefield(player1, new Ornithopter());

        cast(target.getId());

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new RagingGoblin());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Raging Goblin")))
                .isInstanceOf(IllegalStateException.class);
    }
}
