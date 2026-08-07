package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GraspOfTheHieromancerTest extends BaseCardTest {

    private Permanent enchant(Permanent creature) {
        Permanent aura = new Permanent(new GraspOfTheHieromancer());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }

    @Test
    @DisplayName("Enchanted creature gets +1/+1")
    void boostsEnchantedCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        enchant(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Attacking taps a chosen creature the defending player controls")
    void tapsDefendingCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        enchant(attacker);
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only creatures the defending player controls are legal targets")
    void ownCreaturesAreNotLegalTargets() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        enchant(attacker);
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactly(victim.getId())
                .doesNotContain(ownBears.getId(), attacker.getId());
    }

    @Test
    @DisplayName("No target selection when the defending player controls no creature")
    void noTriggerTargetWithoutDefendingCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        enchant(attacker);

        declareAttackers(player1, List.of(0));

        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
    }

    @Test
    @DisplayName("An unattached Grasp grants no attack trigger")
    void unattachedGraspDoesNotTrigger() {
        addCreatureReady(player1, new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new GraspOfTheHieromancer()));
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
    }
}
