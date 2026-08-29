package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DestructiveUrgeTest extends BaseCardTest {

    @Test
    @DisplayName("The damaged player chooses a land to sacrifice")
    void damagedPlayerChoosesLandToSacrifice() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attachDestructiveUrge(player1, attacker);
        attacker.setAttacking(true);

        Permanent mountain = addPermanent(player2, new Mountain());
        Permanent forest = addPermanent(player2, new Forest());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validIds())
                .containsExactlyInAnyOrder(mountain.getId(), forest.getId())
                .doesNotContain(creature.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(mountain.getId()));

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A blocked enchanted creature does not trigger the land sacrifice")
    void blockedCreatureDoesNotTrigger() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attachDestructiveUrge(player1, attacker);
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        addPermanent(player2, new Mountain());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Mountain");
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void attachDestructiveUrge(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new DestructiveUrge());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
    }
}
