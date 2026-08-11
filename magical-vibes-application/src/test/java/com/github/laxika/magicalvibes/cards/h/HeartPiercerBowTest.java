package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HeartPiercerBowTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature attacking deals 1 damage to a defending creature")
    void attackTriggerDealsDamage() {
        Permanent bow = addBowReady(player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        bow.setAttachedTo(attacker.getId());
        Permanent victim = addCreatureReady(player2, new LlanowarElves());

        declareAttackers(player1, List.of(1));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Only creatures controlled by the defending player are legal targets")
    void targetsOnlyDefendingCreatures() {
        Permanent bow = addBowReady(player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        bow.setAttachedTo(attacker.getId());
        Permanent ownCreature = addCreatureReady(player1, new LlanowarElves());
        Permanent defendingCreature = addCreatureReady(player2, new LlanowarElves());

        declareAttackers(player1, List.of(1));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactly(defendingCreature.getId())
                .doesNotContain(ownCreature.getId(), attacker.getId());
    }

    @Test
    @DisplayName("An unattached Bow does not trigger when a creature attacks")
    void unattachedBowDoesNotTrigger() {
        addBowReady(player1);
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new LlanowarElves());

        declareAttackers(player1, List.of(1));

        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
    }

    @Test
    @DisplayName("Equip {1} attaches the Bow to a creature you control")
    void equipAttachesToCreature() {
        Permanent bow = addBowReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(bow.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addBowReady(Player player) {
        Permanent bow = new Permanent(new HeartPiercerBow());
        bow.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(bow);
        return bow;
    }

}
