package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BloodshotCyclops;
import com.github.laxika.magicalvibes.cards.e.ElvishLookout;
import com.github.laxika.magicalvibes.cards.g.GoblinBerserker;
import com.github.laxika.magicalvibes.cards.m.MetathranSoldier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SigilOfSleep.class, BloodshotCyclops.class, GoblinBerserker.class, MetathranSoldier.class,
        ElvishLookout.class})
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

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(target.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.assertOnBattlefield(player2, "Metathran Soldier");
        resolveAllTriggers();

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
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(target.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.assertOnBattlefield(player2, "Metathran Soldier");
        resolveAllTriggers();

        harness.assertInHand(player2, "Metathran Soldier");
        harness.assertNotOnBattlefield(player2, "Metathran Soldier");
    }

    @Test
    @DisplayName("The Aura controller chooses the target even when the enchanted creature damages them")
    void auraControllerChoosesWhenOpponentDealsDamageToThem() {
        Permanent pinger = addCreatureReady(player2, new BloodshotCyclops());
        Permanent sacrifice = addCreatureReady(player2, new GoblinBerserker());
        Permanent target = addCreatureReady(player1, new MetathranSoldier());
        attachSigil(player1, pinger);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.handlePermanentChosen(player2, sacrifice.getId());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.assertOnBattlefield(player1, "Metathran Soldier");
        resolveAllTriggers();

        harness.assertInHand(player1, "Metathran Soldier");
        harness.assertNotOnBattlefield(player1, "Metathran Soldier");
    }

    @Test
    @DisplayName("A creature with shroud cannot be returned when it is the only creature the damaged player controls")
    void shroudCreatureIsNotALegalTarget() {
        Permanent attacker = addCreatureReady(player1, new BloodshotCyclops());
        addCreatureReady(player2, new ElvishLookout());
        attachSigil(player1, attacker);
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player2, "Elvish Lookout");
        assertThat(gd.stack).isEmpty();
    }

    private void attachSigil(Player controller, Permanent creature) {
        Permanent sigil = harness.addToBattlefieldAndReturn(controller, new SigilOfSleep());
        sigil.setAttachedTo(creature.getId());
    }
}
