package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OKagachiVengefulKami.class, FountainOfYouth.class, GrizzlyBears.class, Mountain.class})
class OKagachiVengefulKamiTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a nonland permanent controlled by a player who attacked its controller last turn")
    void exilesQualifyingPlayersNonlandPermanent() {
        Permanent okagachi = addCreatureReady(player1, new OKagachiVengefulKami());
        okagachi.setAttacking(true);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        gd.playersWhoAttackedPlayersLastTurn.put(player1.getId(), new HashSet<>(Set.of(player2.getId())));

        resolveCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(artifact.getId())
                .doesNotContain(land.getId(), ownArtifact.getId());

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Does not trigger for a player who did not attack its controller last turn")
    void doesNotTriggerWithoutQualifyingAttack() {
        Permanent okagachi = addCreatureReady(player1, new OKagachiVengefulKami());
        okagachi.setAttacking(true);
        harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        resolveCombat();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Fountain of Youth");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
