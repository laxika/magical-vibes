package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GhoulishImpetus.class, DarkBanishing.class, GrizzlyBears.class})
class GhoulishImpetusTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+1, deathtouch, and goad")
    void enchantedCreatureGetsStaticEffects() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachGhoulishImpetus(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(als.getMustAttackRequirementCount(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Returns attached at the next end step after the enchanted creature dies")
    void returnsAtNextEndStep() {
        Permanent dyingCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent targetCreature = addCreatureReady(player2, new GrizzlyBears());
        attachGhoulishImpetus(player1, dyingCreature);

        destroyCreature(player2, dyingCreature);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Ghoulish Impetus"));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Ghoulish Impetus");
        assertThat(aura.getAttachedTo()).isEqualTo(targetCreature.getId());
        harness.assertNotInGraveyard(player1, "Ghoulish Impetus");
    }

    private Permanent attachGhoulishImpetus(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new GhoulishImpetus());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }

    private void destroyCreature(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new DarkBanishing()));
        harness.addMana(caster, ManaColor.BLACK, 3);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
