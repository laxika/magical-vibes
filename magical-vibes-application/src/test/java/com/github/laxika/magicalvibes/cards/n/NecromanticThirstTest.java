package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NecromanticThirst.class, GrizzlyBears.class})
class NecromanticThirstTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage presents the optional graveyard target")
    void combatDamagePresentsOptionalGraveyardTarget() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        Permanent creature = addCreatureReady(player1);
        attachNecromanticThirst(player1, creature);
        creature.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
    }

    @Test
    @DisplayName("Choosing a creature card returns it from the graveyard")
    void choosingCreatureReturnsIt() {
        GrizzlyBears deadCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(deadCreature));
        Permanent creature = addCreatureReady(player1);
        attachNecromanticThirst(player1, creature);
        creature.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(deadCreature.getId()));

        resolveAllTriggers();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Choosing no card leaves the graveyard unchanged")
    void choosingNoCardDoesNotReturnCreature() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        Permanent creature = addCreatureReady(player1);
        attachNecromanticThirst(player1, creature);
        creature.setAttacking(true);

        resolveCombat();

        harness.handleMultipleCardsChosen(player1, List.of());

        resolveAllTriggers();

        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private Permanent addCreatureReady(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void attachNecromanticThirst(Player player, Permanent creature) {
        Permanent aura = new Permanent(new NecromanticThirst());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player.getId()).add(aura);
    }
}
