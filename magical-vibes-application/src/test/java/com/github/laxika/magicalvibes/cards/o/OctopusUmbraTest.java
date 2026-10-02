package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.Gigantosaurus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OctopusUmbra.class, Gigantosaurus.class, GrizzlyBears.class})
class OctopusUmbraTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has base power and toughness 8/8")
    void setsEnchantedCreatureBaseStats() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachUmbra(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(8);
    }

    @Test
    @DisplayName("Umbra armor saves the enchanted creature and destroys Octopus Umbra")
    void umbraArmorSavesEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = attachUmbra(creature);
        creature.setMarkedDamage(8);

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature).doesNotContain(aura);
        harness.assertInGraveyard(player1, "Octopus Umbra");
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Attacking may tap a target creature with power 8 or less")
    void attackTriggerTapsEligibleCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attachUmbra(attacker);
        Permanent eligible = addCreatureReady(player2, new GrizzlyBears());
        Permanent tooPowerful = addCreatureReady(player2, new Gigantosaurus());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).contains(eligible.getId()).doesNotContain(tooPowerful.getId());

        harness.handlePermanentChosen(player1, eligible.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(eligible.isTapped()).isTrue();
        assertThat(tooPowerful.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the attack trigger leaves the target untapped")
    void decliningAttackTriggerLeavesTargetUntapped() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attachUmbra(attacker);
        Permanent eligible = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        harness.handlePermanentChosen(player1, eligible.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(eligible.isTapped()).isFalse();
    }

    private Permanent attachUmbra(Permanent creature) {
        Permanent aura = new Permanent(new OctopusUmbra());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }
}
