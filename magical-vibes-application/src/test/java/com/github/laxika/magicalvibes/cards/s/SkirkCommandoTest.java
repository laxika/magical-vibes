package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkirkCommando.class, PrimordialWurm.class})
class SkirkCommandoTest extends BaseCardTest {

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUp() {
        harness.setHand(player1, List.of(new SkirkCommando()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent commando = findPermanent(player1, "Skirk Commando");
        assertThat(commando.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(commando));
        harness.passBothPriorities();

        assertThat(commando.isFaceDown()).isFalse();
    }

    @Test
    void combatDamageTriggerMayDealTwoDamageToDamagedPlayersCreature() {
        Permanent commando = addCreatureReady(player1, new SkirkCommando());
        commando.setAttacking(true);
        Permanent target = addCreatureReady(player2, new PrimordialWurm());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(target.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void decliningCombatDamageTriggerDealsNoAdditionalDamage() {
        Permanent commando = addCreatureReady(player1, new SkirkCommando());
        commando.setAttacking(true);
        Permanent target = addCreatureReady(player2, new PrimordialWurm());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getMarkedDamage()).isZero();
    }
}
