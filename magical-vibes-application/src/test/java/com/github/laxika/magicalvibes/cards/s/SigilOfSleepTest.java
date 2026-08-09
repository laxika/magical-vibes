package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SigilOfSleepTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage prompts to return a creature controlled by the damaged player")
    void combatDamageReturnsDamagedPlayersCreature() {
        Permanent enchantedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        attachSigil(player1, enchantedCreature);
        enchantedCreature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(target.getId());

        harness.handlePermanentChosen(player1, target.getId());

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Noncombat damage also triggers the bounce")
    void noncombatDamageTriggersBounce() {
        Permanent pinger = addReadySorcerer(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        attachSigil(player1, pinger);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(target.getId());

        harness.handlePermanentChosen(player1, target.getId());

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    private Permanent addReadySorcerer(Player player) {
        Permanent sorcerer = new Permanent(new ProdigalSorcerer());
        sorcerer.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(sorcerer);
        return sorcerer;
    }

    private void attachSigil(Player controller, Permanent creature) {
        Permanent sigil = new Permanent(new SigilOfSleep());
        sigil.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(sigil);
    }
}
