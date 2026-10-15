package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FabledPassage;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HavocJester.class, GrizzlyBears.class, LlanowarElves.class, Hobblefiend.class, FabledPassage.class})
class HavocJesterTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing your permanent triggers 1 damage to any target")
    void sacrificeDealsDamageToTargetPlayer() {
        addCreatureReady(player1, new HavocJester());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        int lifeBefore = gd.getLife(player2.getId());

        sacrifice(bears);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Sacrifice trigger can target a creature")
    void sacrificeDealsDamageToTargetCreature() {
        addCreatureReady(player1, new HavocJester());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new LlanowarElves());
        UUID targetId = target.getId();

        sacrifice(bears);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(targetId, player2.getId());
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Opponent sacrificing a permanent does not trigger Havoc Jester")
    void opponentSacrificeDoesNotTrigger() {
        addCreatureReady(player1, new HavocJester());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        sacrificeForPlayer(bears, player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Havoc Jester triggers when sacrificed to another creature's ability")
    void sacrificingJesterItselfStillDealsDamage() {
        addCreatureReady(player1, new Hobblefiend());
        addCreatureReady(player1, new HavocJester());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Havoc Jester");
        harness.assertNotOnBattlefield(player1, "Havoc Jester");
        harness.assertLife(player2, lifeBefore - 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing a land triggers Havoc Jester and can damage its controller")
    void sacrificingLandCanDamageController() {
        addCreatureReady(player1, new HavocJester());
        harness.addToBattlefield(player1, new FabledPassage());
        harness.setLibrary(player1, List.of(new HavocJester()));
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player1.getId(), player2.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore - 1);
        harness.assertInGraveyard(player1, "Fabled Passage");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void sacrifice(Permanent permanent) {
        sacrificeForPlayer(permanent, player1);
    }

    private void sacrificeForPlayer(Permanent permanent, com.github.laxika.magicalvibes.model.Player player) {
        Card card = permanent.getCard();
        gd.playerBattlefields.get(player.getId()).remove(permanent);
        gd.playerGraveyards.get(player.getId()).add(card);
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkAllyPermanentSacrificedTriggers(gd, player.getId(), card));
    }
}
