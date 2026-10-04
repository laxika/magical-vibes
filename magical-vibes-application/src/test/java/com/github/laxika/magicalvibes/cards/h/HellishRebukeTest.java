package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.Confiscate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HellishRebuke.class, GrizzlyBears.class, ProdigalSorcerer.class, Confiscate.class, Unsummon.class})
class HellishRebukeTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent permanent that damages the caster is sacrificed and its controller loses 2 life")
    void punishesCombatDamageToCaster() {
        harness.setHand(player1, List.of(new HellishRebuke()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        attacker.setAttacking(true);
        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The granted ability also triggers from noncombat damage")
    void punishesNoncombatDamageToCaster() {
        harness.setHand(player1, List.of(new HellishRebuke()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        addCreatureReady(player2, new ProdigalSorcerer());

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Prodigal Sorcerer");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The effect lasts only through the end of turn")
    void grantWearsOffAtEndOfTurn() {
        harness.setHand(player1, List.of(new HellishRebuke()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        Permanent pinger = addCreatureReady(player2, new ProdigalSorcerer());

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(pinger);
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Permanents entering after Hellish Rebuke resolves do not gain the ability")
    void doesNotAffectLaterPermanents() {
        harness.setHand(player1, List.of(new HellishRebuke()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        addCreatureReady(player2, new ProdigalSorcerer());
        harness.activateAbility(player2, 0, null, player1.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Prodigal Sorcerer");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Two cast Hellish Rebukes grant two independent triggers")
    void multipleRebukesEachCauseLifeLoss() {
        addCreatureReady(player2, new ProdigalSorcerer());
        harness.setHand(player1, List.of(new HellishRebuke(), new HellishRebuke()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.activateAbility(player2, 0, null, player1.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Prodigal Sorcerer");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Life is still lost when the source leaves before the trigger resolves")
    void lifeLossDoesNotRequireSacrifice() {
        Permanent pinger = addCreatureReady(player2, new ProdigalSorcerer());
        harness.setHand(player1, List.of(new HellishRebuke(), new Unsummon()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.castInstant(player1, 0, pinger.getId());
        resolveAllTriggers();

        harness.assertInHand(player2, "Prodigal Sorcerer");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A permanent retains the grant after changing control and still triggers for damage to the caster")
    void retainedGrantTriggersForDamageToCasterUnderNewController() {
        Permanent pinger = addCreatureReady(player2, new ProdigalSorcerer());
        harness.setHand(player1, List.of(new HellishRebuke(), new Confiscate()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0, pinger.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Prodigal Sorcerer");
        pinger.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, player1.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Prodigal Sorcerer");
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The caster's own permanents do not gain the ability")
    void doesNotGrantAbilityToCasterPermanents() {
        addCreatureReady(player1, new ProdigalSorcerer());
        harness.setHand(player1, List.of(new HellishRebuke()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Prodigal Sorcerer");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }
}
