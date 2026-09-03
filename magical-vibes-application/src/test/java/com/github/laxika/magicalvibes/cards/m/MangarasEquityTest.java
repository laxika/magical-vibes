package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FemerefHealer;
import com.github.laxika.magicalvibes.cards.f.FemerefScouts;
import com.github.laxika.magicalvibes.cards.s.ShadowGuildmage;
import com.github.laxika.magicalvibes.cards.t.TalruumMinotaur;
import com.github.laxika.magicalvibes.cards.w.WallOfRoots;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({
        MangarasEquity.class,
        FemerefHealer.class,
        FemerefScouts.class,
        ShadowGuildmage.class,
        TalruumMinotaur.class,
        WallOfRoots.class
})
class MangarasEquityTest extends BaseCardTest {

    private Permanent addEquity(Player controller, CardColor chosenColor) {
        Permanent equity = harness.addToBattlefieldAndReturn(controller, new MangarasEquity());
        equity.setChosenColor(chosenColor);
        return equity;
    }

    private Permanent attackWith(Card creature) {
        Permanent attacker = addCreatureReady(player1, creature);
        attacker.setAttacking(true);
        return attacker;
    }

    private Permanent addBlocker(Card creature) {
        Permanent blocker = addCreatureReady(player2, creature);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        return blocker;
    }

    private void resolveCombatAndTriggers() {
        resolveCombat();
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Entering the battlefield offers only black and red")
    void entersWithRestrictedColorChoice() {
        harness.setHand(player1, List.of(new MangarasEquity()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("BLACK", "RED");

        harness.handleListChoice(player1, CardColor.BLACK.name());
    }

    @Test
    @DisplayName("A creature of the chosen color damaging you takes that much damage back")
    void combatDamageToControllerReflected() {
        addEquity(player2, CardColor.RED);
        Permanent attacker = attackWith(new TalruumMinotaur());

        resolveCombatAndTriggers();

        harness.assertLife(player2, 17);
        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("A creature of another color damaging you is not punished")
    void otherColorNotReflected() {
        addEquity(player2, CardColor.BLACK);
        Permanent attacker = attackWith(new TalruumMinotaur());

        resolveCombatAndTriggers();

        harness.assertLife(player2, 17);
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A creature of the chosen color damaging a white creature you control is punished")
    void damageToWhiteCreatureReflected() {
        addEquity(player2, CardColor.RED);
        Permanent blocker = addBlocker(new FemerefScouts());
        attackWith(new TalruumMinotaur());

        resolveCombatAndTriggers();

        harness.assertLife(player2, 20);
        assertThat(blocker.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        harness.assertInGraveyard(player1, "Talruum Minotaur");
    }

    @Test
    @DisplayName("Damage to a non-white creature you control is not punished")
    void damageToNonWhiteCreatureNotReflected() {
        addEquity(player2, CardColor.RED);
        addBlocker(new WallOfRoots());
        Permanent attacker = attackWith(new TalruumMinotaur());

        resolveCombatAndTriggers();

        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Damage to a white creature is reflected even if that creature dies")
    void damageToWhiteCreatureThatDiesIsReflected() {
        addEquity(player2, CardColor.RED);
        addBlocker(new FemerefHealer());
        attackWith(new TalruumMinotaur());

        resolveCombatAndTriggers();

        harness.assertInGraveyard(player2, "Femeref Healer");
        harness.assertInGraveyard(player1, "Talruum Minotaur");
    }

    @Test
    @DisplayName("Noncombat damage to you from a chosen-color creature is reflected")
    void nonCombatDamageToControllerReflected() {
        addEquity(player2, CardColor.BLACK);
        addCreatureReady(player1, new ShadowGuildmage());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Shadow Guildmage");
    }

    @Test
    @DisplayName("Paying {1}{W} at upkeep keeps it on the battlefield")
    void payAtUpkeepKeepsIt() {
        addEquity(player1, CardColor.RED);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Mangara's Equity");
    }

    @Test
    @DisplayName("Declining to pay at upkeep sacrifices it")
    void declineAtUpkeepSacrificesIt() {
        addEquity(player1, CardColor.RED);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Mangara's Equity");
    }
}
