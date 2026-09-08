package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.k.KamahlPitFighter;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VenomousFangsTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys another creature dealt noncombat damage by the enchanted creature")
    void destroysAnotherCreatureAfterNoncombatDamage() {
        Permanent source = addCreatureReady(player1, new ZuranSpellcaster());
        Permanent target = addCreatureReady(player2, new HillGiant());
        attachFangs(player2, source);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Zuran Spellcaster");
    }

    @Test
    @DisplayName("Does not trigger when the enchanted creature deals damage to a player")
    void doesNotTriggerOnDamageToPlayer() {
        Permanent source = addCreatureReady(player1, new ZuranSpellcaster());
        addCreatureReady(player2, new HillGiant());
        attachFangs(player2, source);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Does not destroy the enchanted creature when it damages itself")
    void doesNotDestroyEnchantedCreatureWhenItDamagesItself() {
        KamahlPitFighter kamahl = new KamahlPitFighter();
        kamahl.setToughness(4);
        Permanent source = addCreatureReady(player1, kamahl);
        attachFangs(player2, source);

        harness.activateAbility(player1, 0, 0, null, source.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Kamahl, Pit Fighter");
    }

    @Test
    @DisplayName("Combat trigger is collected before the enchanted creature and Aura die")
    void combatTriggerSurvivesSourceAndAuraLeaving() {
        Permanent source = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new HillGiant());
        attachFangs(player2, source);
        source.setAttacking(true);
        target.setBlocking(true);
        target.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Venomous Fangs");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void attachFangs(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new VenomousFangs());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
    }
}
