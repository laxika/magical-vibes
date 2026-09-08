package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeepfathomSkulker.class, Forest.class, FountainOfYouth.class, GrizzlyBears.class})
class DeepfathomSkulkerTest extends BaseCardTest {

    @Test
    @DisplayName("A creature you control dealing combat damage presents a may-draw choice")
    void combatDamagePresentsMayDrawChoice() {
        addReadyPermanent(player1, new DeepfathomSkulker());
        Permanent attacker = addReadyPermanent(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.setLibrary(player1, List.of(new Forest()));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the combat-damage trigger draws a card")
    void acceptingCombatDamageTriggerDrawsCard() {
        addReadyPermanent(player1, new DeepfathomSkulker());
        Permanent attacker = addReadyPermanent(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof Forest);
    }

    @Test
    @DisplayName("The ability makes a target creature unblockable for the turn")
    void abilityMakesTargetCreatureUnblockable() {
        addReadyPermanent(player1, new DeepfathomSkulker());
        Permanent attacker = addReadyPermanent(player1, new GrizzlyBears());
        addReadyPermanent(player2, new GrizzlyBears());
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.isCantBeBlocked()).isTrue();
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void abilityCannotTargetNoncreature() {
        addReadyPermanent(player1, new DeepfathomSkulker());
        Permanent target = addReadyPermanent(player2, new FountainOfYouth());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
