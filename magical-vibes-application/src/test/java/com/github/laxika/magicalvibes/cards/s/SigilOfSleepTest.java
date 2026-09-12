package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BloodshotCyclops;
import com.github.laxika.magicalvibes.cards.g.GoblinBerserker;
import com.github.laxika.magicalvibes.cards.m.MetathranSoldier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SigilOfSleep.class, BloodshotCyclops.class, GoblinBerserker.class, MetathranSoldier.class})
class SigilOfSleepTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Sigil of Sleep attaches it to the target creature")
    void castingAttachesToCreature() {
        Permanent creature = addCreatureReady(player2, new MetathranSoldier());

        harness.setHand(player1, List.of(new SigilOfSleep()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof SigilOfSleep
                        && permanent.isAttached()
                        && permanent.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Combat damage prompts to return a creature controlled by the damaged player")
    void combatDamageReturnsDamagedPlayersCreature() {
        Permanent enchantedCreature = addCreatureReady(player1, new BloodshotCyclops());
        Permanent target = addCreatureReady(player2, new MetathranSoldier());
        attachSigil(player1, enchantedCreature);
        enchantedCreature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(target.getId());

        harness.handlePermanentChosen(player1, target.getId());

        harness.assertInHand(player2, "Metathran Soldier");
        harness.assertNotOnBattlefield(player2, "Metathran Soldier");
    }

    @Test
    @DisplayName("Noncombat damage also triggers the bounce")
    void noncombatDamageTriggersBounce() {
        Permanent pinger = addCreatureReady(player1, new BloodshotCyclops());
        Permanent sacrifice = addCreatureReady(player1, new GoblinBerserker());
        Permanent target = addCreatureReady(player2, new MetathranSoldier());
        attachSigil(player1, pinger);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, sacrifice.getId());
        resolveAllTriggers();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(target.getId());

        harness.handlePermanentChosen(player1, target.getId());

        harness.assertInHand(player2, "Metathran Soldier");
        harness.assertNotOnBattlefield(player2, "Metathran Soldier");
    }

    private void attachSigil(Player controller, Permanent creature) {
        Permanent sigil = new Permanent(new SigilOfSleep());
        sigil.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(sigil);
    }
}
