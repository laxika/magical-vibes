package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
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

@CardUsed({DeflectingPalm.class, GoblinPiker.class, ProdigalPyromancer.class})
class DeflectingPalmTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Deflecting Palm prompts for a source choice")
    void resolvingPromptsForSourceChoice() {
        castPalm(player1);
        addCreatureReady(player2, new GoblinPiker());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Prevents the chosen source's damage and damages its controller")
    void preventsDamageAndDamagesSourceController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        castPalm(player1);
        Permanent goblin = addCreatureReady(player2, new GoblinPiker());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, goblin.getId());

        goblin.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Prevents the chosen source's noncombat damage and damages its controller")
    void preventsNoncombatDamageAndDamagesSourceController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        castPalm(player1);
        Permanent piker = addCreatureReady(player2, new ProdigalPyromancer());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, piker.getId());

        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.activateAbility(player2, indexOf(player2, piker), null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("A different source still deals damage")
    void differentSourceStillDealsDamage() {
        harness.setLife(player1, 20);
        castPalm(player1);
        Permanent chosen = addCreatureReady(player2, new GoblinPiker());
        Permanent other = addCreatureReady(player2, new GoblinPiker());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        other.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Only the next damage event from the chosen source is prevented")
    void onlyNextDamageEventIsPrevented() {
        Permanent source = addCreatureReady(player2, new ProdigalPyromancer());
        castPalm(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.activateAbility(player2, indexOf(player2, source), null, player1.getId());
        harness.passBothPriorities();
        source.untap();
        harness.clearPriorityPassed();
        harness.activateAbility(player2, indexOf(player2, source), null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("A departed source referenced by an ability on the stack can be chosen")
    void departedAbilitySourceCanBeChosen() {
        Permanent source = addCreatureReady(player2, new ProdigalPyromancer());
        addCreatureReady(player2, new GoblinPiker());
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, indexOf(player2, source), null, player1.getId());
        gd.playerBattlefields.get(player2.getId()).remove(source);
        harness.setGraveyard(player2, List.of(source.getCard()));
        castPalm(player1);
        harness.passBothPriorities();

        var choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(source.getId());
        harness.handlePermanentChosen(player1, source.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Damage dealt by Palm after combat can be prevented by another Palm")
    void anotherPalmCanPreventReturnedCombatDamage() {
        Permanent attacker = addCreatureReady(player2, new GoblinPiker());
        DeflectingPalm firstPalm = new DeflectingPalm();
        harness.setHand(player1, List.of(firstPalm));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0);
        castPalm(player2);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, firstPalm.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, attacker.getId());

        attacker.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    private void castPalm(Player player) {
        harness.setHand(player, List.of(new DeflectingPalm()));
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.castInstant(player, 0);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
