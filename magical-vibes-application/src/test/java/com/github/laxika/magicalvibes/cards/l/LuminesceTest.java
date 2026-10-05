package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.m.MassOfGhouls;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Luminesce.class, DuskImp.class, GoblinPiker.class, GrizzlyBears.class,
        Hurricane.class, MassOfGhouls.class, Shock.class, SuntailHawk.class, YouthfulKnight.class})
class LuminesceTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Luminesce puts it on the stack")
    void castingPutsItOnStack() {
        Luminesce luminesce = new Luminesce();
        harness.setHand(player1, List.of(luminesce));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(luminesce);
    }

    @Test
    @DisplayName("Cannot cast Luminesce without enough mana")
    void cannotCastWithoutMana() {
        harness.setHand(player1, List.of(new Luminesce()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Resolving Luminesce adds black and red to prevented colors")
    void resolvingAddsPreventedColors() {
        harness.setHand(player1, List.of(new Luminesce()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.preventDamageFromColors).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.RED);
    }

    @Test
    @DisplayName("Luminesce goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Luminesce luminesce = new Luminesce();
        harness.setHand(player1, List.of(luminesce));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(harness.getGameData().playerGraveyards.get(player1.getId())).contains(luminesce);
    }

    // ===== Combat damage prevention - black source =====

    @Test
    @DisplayName("Prevents combat damage from black creature to player")
    void preventsBlackCreatureDamageToPlayer() {
        harness.setLife(player2, 20);
        harness.getGameData().preventDamageFromColors.add(CardColor.BLACK);
        harness.getGameData().preventDamageFromColors.add(CardColor.RED);

        Permanent attacker = addCreatureReady(player1, new DuskImp());
        attacker.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Prevents combat damage from black creature to blocking creature")
    void preventsBlackCreatureDamageToBlocker() {
        harness.getGameData().preventDamageFromColors.add(CardColor.BLACK);
        harness.getGameData().preventDamageFromColors.add(CardColor.RED);

        Permanent attacker = addCreatureReady(player1, new MassOfGhouls());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new SuntailHawk());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Black attacker's 5 damage is prevented, so the blocker survives.
        harness.assertOnBattlefield(player2, "Suntail Hawk");
        // The blocker still deals 1 damage to the attacker, but the attacker survives.
        harness.assertOnBattlefield(player1, "Mass of Ghouls");
    }

    // ===== Combat damage prevention - red source =====

    @Test
    @DisplayName("Prevents combat damage from red creature to player")
    void preventsRedCreatureDamageToPlayer() {
        harness.setLife(player2, 20);
        harness.getGameData().preventDamageFromColors.add(CardColor.BLACK);
        harness.getGameData().preventDamageFromColors.add(CardColor.RED);

        Permanent attacker = addCreatureReady(player1, new GoblinPiker());
        attacker.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== Non-prevented colors still deal damage =====

    @Test
    @DisplayName("Does not prevent combat damage from green creature")
    void doesNotPreventGreenCreatureDamage() {
        harness.setLife(player2, 20);
        harness.getGameData().preventDamageFromColors.add(CardColor.BLACK);
        harness.getGameData().preventDamageFromColors.add(CardColor.RED);

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat();

        // Green creature's 2 damage goes through
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not prevent combat damage from white creature")
    void doesNotPreventWhiteCreatureDamage() {
        harness.setLife(player2, 20);
        harness.getGameData().preventDamageFromColors.add(CardColor.BLACK);
        harness.getGameData().preventDamageFromColors.add(CardColor.RED);

        Permanent attacker = addCreatureReady(player1, new YouthfulKnight());
        attacker.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Mixed combat =====

    @Test
    @DisplayName("In mixed combat, only prevents damage from black/red attackers")
    void mixedCombatOnlyPreventsBlackRedDamage() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Luminesce()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0);

        // Black attacker (prevented)
        Permanent blackAttacker = addCreatureReady(player1, new DuskImp());
        blackAttacker.setAttacking(true);

        // Green attacker (not prevented)
        Permanent greenAttacker = addCreatureReady(player1, new GrizzlyBears());
        greenAttacker.setAttacking(true);

        resolveCombat();

        // Only green's 2 damage goes through, black's 2 is prevented
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Spell damage prevention =====

    @Test
    @DisplayName("Does not prevent spell damage from non-prevented color (Hurricane is green)")
    void doesNotPreventGreenSpellDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.getGameData().preventDamageFromColors.add(CardColor.BLACK);
        harness.getGameData().preventDamageFromColors.add(CardColor.RED);

        harness.setHand(player1, List.of(new Hurricane()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player1, 0, 2);

        // Hurricane is green, so damage is not prevented
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Prevents red noncombat damage after Luminesce resolves")
    void preventsRedSpellDamage() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Luminesce()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== End of turn cleanup =====

    @Test
    @DisplayName("Prevented colors are cleared at end of turn")
    void preventedColorsClearedAtEndOfTurn() {
        harness.getGameData().preventDamageFromColors.add(CardColor.BLACK);
        harness.getGameData().preventDamageFromColors.add(CardColor.RED);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.preventDamageFromColors).isEmpty();
    }

    @Test
    @DisplayName("Prevents repeated opposing red spells against players and creatures")
    void preventsRepeatedOpposingRedDamage() {
        Permanent hawk = addCreatureReady(player1, new SuntailHawk());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Luminesce()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0);

        harness.setHand(player2, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, hawk.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Suntail Hawk");
        assertThat(hawk.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Prevents black blocker damage while green attacker damage is dealt")
    void preventsBlackBlockerDamage() {
        harness.setHand(player1, List.of(new Luminesce()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0);

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new MassOfGhouls());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Mass of Ghouls");
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Red spell damage resumes on the next turn")
    void redDamageResumesNextTurn() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Luminesce()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
    }
}

