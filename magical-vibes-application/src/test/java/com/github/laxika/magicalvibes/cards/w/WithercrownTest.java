package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WithercrownTest extends BaseCardTest {

    @Test
    void setsOnlyEnchantedCreatureBasePowerToZero() {
        Permanent creature = addCreature(player2, new AirElemental());
        attachWithercrown(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    void decliningToSacrificeEnchantedCreatureLosesOneLife() {
        Permanent creature = addCreature(player2, new GrizzlyBears());
        attachWithercrown(creature);
        int lifeBefore = gd.getLife(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    void sacrificingEnchantedCreatureDoesNotSacrificeAnotherCreature() {
        Permanent creature = addCreature(player2, new GrizzlyBears());
        Permanent otherCreature = addCreature(player2, new GrizzlyBears());
        attachWithercrown(creature);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(otherCreature);
    }

    @Test
    void auraControllerDoesNotGetEnchantedCreatureTrigger() {
        Permanent creature = addCreature(player2, new GrizzlyBears());
        attachWithercrown(creature);
        int lifeBefore = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player,
                                  com.github.laxika.magicalvibes.model.Card card) {
        Permanent creature = new Permanent(card);
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private void attachWithercrown(Permanent creature) {
        Permanent aura = new Permanent(new Withercrown());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
    }
}
