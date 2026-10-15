package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RancidRats;
import com.github.laxika.magicalvibes.cards.t.TrueFaithCenser;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MorkrutNecropod.class, GrizzlyBears.class, GiantSpider.class, Mountain.class,
        RancidRats.class, TrueFaithCenser.class})
class MorkrutNecropodTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking sacrifices another creature")
    void attackingSacrificesAnotherCreature() {
        addCreatureReady(player1, new MorkrutNecropod());
        harness.addToBattlefield(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Morkrut Necropod");
    }

    @Test
    void canSacrificeAnotherNecropod() {
        Permanent attacker = addCreatureReady(player1, new MorkrutNecropod());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new MorkrutNecropod());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker).doesNotContain(other);
        harness.assertInGraveyard(player1, "Morkrut Necropod");
    }

    @Test
    void doesNotSacrificeArtifactsOrOpponentsPermanents() {
        addCreatureReady(player1, new MorkrutNecropod());
        harness.addToBattlefield(player1, new TrueFaithCenser());
        harness.addToBattlefield(player2, new Mountain());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Morkrut Necropod");
        harness.assertOnBattlefield(player1, "True-Faith Censer");
        harness.assertOnBattlefield(player2, "Mountain");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void attackTriggerSacrificesLand() {
        addCreatureReady(player1, new MorkrutNecropod());
        harness.addToBattlefield(player1, new Mountain());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mountain");
        harness.assertOnBattlefield(player1, "Morkrut Necropod");
    }

    @Test
    void menaceRejectsOneBlocker() {
        Permanent attacker = addCreatureReady(player1, new MorkrutNecropod());
        attacker.setAttacking(true);
        harness.addToBattlefield(player2, new RancidRats());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked except by two or more creatures");
    }
    @Test
    @DisplayName("Blocking sacrifices a land")
    void blockingSacrificesLand() {
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        addCreatureReady(player2, new MorkrutNecropod());
        harness.addToBattlefield(player2, new Mountain());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertOnBattlefield(player2, "Morkrut Necropod");
    }

    @Test
    @DisplayName("Controller chooses which other creature or land to sacrifice")
    void choosesOtherCreatureOrLand() {
        addCreatureReady(player1, new MorkrutNecropod());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(land.getId()));

        harness.assertOnBattlefield(player1, "Morkrut Necropod");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Mountain");
    }

    @Test
    @DisplayName("Does nothing when there is no other creature or land")
    void noOtherCreatureOrLandIsHarmless() {
        addCreatureReady(player1, new MorkrutNecropod());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Morkrut Necropod");
    }

}
