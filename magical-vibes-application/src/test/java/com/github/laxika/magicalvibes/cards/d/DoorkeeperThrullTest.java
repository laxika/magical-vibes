package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RecklessFireweaver;
import com.github.laxika.magicalvibes.cards.s.SurgeNode;
import com.github.laxika.magicalvibes.cards.s.SuturePriest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoorkeeperThrull.class, GrizzlyBears.class, SuturePriest.class,
        RecklessFireweaver.class, SurgeNode.class})
class DoorkeeperThrullTest extends BaseCardTest {

    @Test
    @DisplayName("A creature entering does not cause creature-enter triggers")
    void suppressesCreatureEnteringTriggers() {
        harness.addToBattlefield(player1, new DoorkeeperThrull());
        harness.addToBattlefield(player1, new SuturePriest());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A noncreature artifact entering does not cause artifact-enter triggers")
    void suppressesNoncreatureArtifactEnteringTriggers() {
        harness.addToBattlefield(player1, new DoorkeeperThrull());
        harness.addToBattlefield(player1, new RecklessFireweaver());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SurgeNode()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }
}
