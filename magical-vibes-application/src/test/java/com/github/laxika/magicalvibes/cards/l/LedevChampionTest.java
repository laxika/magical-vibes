package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.v.VernadiShieldmate;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LedevChampion.class, VernadiShieldmate.class})
class LedevChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking prompts to tap untapped creatures and boosts Ledev Champion")
    void attackTriggerTapsCreaturesAndBoostsChampion() {
        Permanent champion = addCreatureReady(player1, new LedevChampion());
        Permanent firstCreature = addCreatureReady(player1, new VernadiShieldmate());
        Permanent secondCreature = addCreatureReady(player1, new VernadiShieldmate());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(champion)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(4);
        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining the attack trigger leaves Ledev Champion unchanged")
    void choosingNoCreaturesDoesNotBoostChampion() {
        Permanent champion = addCreatureReady(player1, new LedevChampion());
        Permanent creature = addCreatureReady(player1, new VernadiShieldmate());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(champion)));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(2);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void attackBoostWearsOffAtEndOfTurn() {
        Permanent champion = addCreatureReady(player1, new LedevChampion());
        Permanent creature = addCreatureReady(player1, new VernadiShieldmate());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(champion)));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(3);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(2);
    }

    @Test
    @DisplayName("The activated ability creates a white Soldier with lifelink")
    void activatedAbilityCreatesLifelinkSoldier() {
        addCreatureReady(player1, new LedevChampion());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent soldier = findPermanent(player1, "Soldier");
        assertThat(soldier.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(soldier.getCard().getPower()).isEqualTo(1);
        assertThat(soldier.getCard().getToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, soldier, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("The trigger can tap a summoning-sick creature and a vigilant attacker")
    void canTapSummoningSickCreatureAndVigilantAttacker() {
        Permanent champion = addCreatureReady(player1, new LedevChampion());
        Permanent vigilantAttacker = addCreatureReady(player1, new VernadiShieldmate());
        Permanent newCreature = harness.addToBattlefieldAndReturn(player1, new VernadiShieldmate());
        newCreature.setSummoningSick(true);
        Permanent tappedCreature = addCreatureReady(player1, new VernadiShieldmate());
        tappedCreature.setTapped(true);
        Permanent opposingCreature = addCreatureReady(player2, new VernadiShieldmate());

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(vigilantAttacker.getId(), newCreature.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(vigilantAttacker.getId(), newCreature.getId()));

        assertThat(vigilantAttacker.isTapped()).isTrue();
        assertThat(vigilantAttacker.isAttacking()).isTrue();
        assertThat(newCreature.isTapped()).isTrue();
        assertThat(opposingCreature.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(4);
    }

    @Test
    @DisplayName("Attacking with no untapped creatures resolves without a choice or boost")
    void noUntappedCreaturesDoesNotBoostChampion() {
        Permanent champion = addCreatureReady(player1, new LedevChampion());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(2);
    }

    @Test
    @DisplayName("A tapped summoning-sick Champion can activate repeatedly")
    void tappedSummoningSickChampionCanCreateMultipleSoldiers() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new LedevChampion());
        champion.setTapped(true);
        champion.setSummoningSick(true);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(2);
        assertThat(champion.isTapped()).isTrue();
    }
}
