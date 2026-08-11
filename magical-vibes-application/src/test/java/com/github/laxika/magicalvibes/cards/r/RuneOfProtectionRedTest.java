package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RuneOfProtectionRedTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability prompts for a red source choice")
    void resolvingAbilityPromptsForRedSource() {
        addReadyRune(player1);
        addReadyRedCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Prevents the next combat damage from the chosen red source")
    void preventsNextCombatDamageFromChosenSource() {
        harness.setLife(player1, 20);
        addReadyRune(player1);
        Permanent goblin = addReadyRedCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, goblin.getId());

        goblin.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Only the chosen red source is prevented")
    void differentRedSourceStillDealsDamage() {
        harness.setLife(player1, 20);
        addReadyRune(player1);
        Permanent chosen = addReadyRedCreature(player2);
        Permanent other = addReadyRedCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        other.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Non-red permanents are not valid source choices")
    void nonRedSourceNotValid() {
        addReadyRune(player1);
        addReadyNonRedCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        RuneOfProtectionRed rune = new RuneOfProtectionRed();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(rune));
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(rune);
        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
    }

    private Permanent addReadyRune(Player player) {
        Permanent perm = new Permanent(new RuneOfProtectionRed());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyRedCreature(Player player) {
        Permanent perm = new Permanent(new RagingGoblin());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyNonRedCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
