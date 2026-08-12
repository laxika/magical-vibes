package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BogWraith;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShadowbaneTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Shadowbane prompts for a source choice")
    void resolvingPromptsForSourceChoice() {
        castShadowbane(player1);
        addReady(player2, new BogWraith());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Prevents the chosen black source's damage to you and gains that much life")
    void preventsDamageToControllerAndGainsLifeFromBlackSource() {
        harness.setLife(player1, 20);
        castShadowbane(player1);
        Permanent wraith = addReady(player2, new BogWraith());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, wraith.getId());

        wraith.setAttacking(true);
        resolveCombat(player2);

        // 3 damage prevented, 3 life gained because Bog Wraith is black
        harness.assertLife(player1, 23);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Prevents a non-black chosen source's damage but grants no life")
    void preventsDamageWithoutLifeGainFromNonBlackSource() {
        harness.setLife(player1, 20);
        castShadowbane(player1);
        Permanent giant = addReady(player2, new HillGiant());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, giant.getId());

        giant.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Prevents the chosen source's damage to a creature you control")
    void preventsDamageToControlledCreature() {
        harness.setLife(player1, 20);
        castShadowbane(player1);
        Permanent wraith = addReady(player2, new BogWraith());
        Permanent bears = addReady(player1, new GrizzlyBears());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, wraith.getId());

        wraith.setAttacking(true);
        bears.setBlocking(true);
        bears.addBlockingTarget(0);
        resolveCombat(player2);

        // All 3 damage Bog Wraith assigns to the blocker is prevented, so the 2/2 survives
        // and its controller gains 3 life because Bog Wraith is black.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("A different source still deals damage; the shield is untouched")
    void differentSourceStillDealsDamage() {
        harness.setLife(player1, 20);
        castShadowbane(player1);
        Permanent chosen = addReady(player2, new BogWraith());
        Permanent other = addReady(player2, new HillGiant());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        other.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 17);
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.sourceId().equals(chosen.getId()));
    }

    @Test
    @DisplayName("Shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        castShadowbane(player1);
        Permanent wraith = addReady(player2, new BogWraith());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, wraith.getId());

        assertThat(gd.playerSourceNextDamageShields).isNotEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    private void castShadowbane(Player player) {
        harness.setHand(player, List.of(new Shadowbane()));
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.castInstant(player, 0);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
