package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.l.LeadenMyr;
import com.github.laxika.magicalvibes.cards.m.Myrsmith;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.g.GalvanicBlast;
import com.github.laxika.magicalvibes.cards.k.KothOfTheHammer;
import com.github.laxika.magicalvibes.cards.o.OriginSpellbomb;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AbunaAcolyte.class, Myrsmith.class, LeadenMyr.class, GalvanicBlast.class,
        OriginSpellbomb.class, KothOfTheHammer.class})
class AbunaAcolyteTest extends BaseCardTest {

    private void addAcolyteReady() {
        Permanent acolyte = harness.addToBattlefieldAndReturn(player1, new AbunaAcolyte());
        acolyte.setSummoningSick(false);
    }

    @Test
    @DisplayName("Ability 1 adds 1 prevention shield to target creature")
    void ability1PreventsOnCreature() {
        addAcolyteReady();
        harness.addToBattlefield(player2, new Myrsmith());

        UUID targetId = harness.getPermanentId(player2, "Myrsmith");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent myrsmith = findPermanent(player2, "Myrsmith");
        assertThat(myrsmith.getDamagePreventionShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability 1 adds 1 prevention shield to target player")
    void ability1PreventsOnPlayer() {
        addAcolyteReady();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability 1 prevention shield saves creature from lethal combat damage")
    void ability1ShieldSavesCreature() {
        // Set up defender (1/1) with 1 prevention shield, blocking a 1/1 attacker
        Permanent defender = harness.addToBattlefieldAndReturn(player2, new AbunaAcolyte());
        defender.setSummoningSick(false);
        defender.setDamagePreventionShield(1);
        defender.setBlocking(true);
        defender.addBlockingTarget(0);

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new AbunaAcolyte());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();

        // Defender took 1 damage - 1 prevented = 0 effective → survives
        harness.assertOnBattlefield(player2, "Abuna Acolyte");
    }

    @Test
    @DisplayName("Ability 2 adds 2 prevention shield to target artifact creature")
    void ability2PreventsOnArtifactCreature() {
        addAcolyteReady();
        harness.addToBattlefield(player1, new LeadenMyr());

        UUID targetId = harness.getPermanentId(player1, "Leaden Myr");
        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        Permanent myr = findPermanent(player1, "Leaden Myr");
        assertThat(myr.getDamagePreventionShield()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability 2 cannot target non-artifact creature")
    void ability2CannotTargetNonArtifactCreature() {
        addAcolyteReady();
        harness.addToBattlefield(player2, new Myrsmith());

        UUID targetId = harness.getPermanentId(player2, "Myrsmith");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void respectsSummoningSickness() {
        harness.addToBattlefield(player1, new AbunaAcolyte());
        harness.addToBattlefield(player2, new Myrsmith());

        UUID targetId = harness.getPermanentId(player2, "Myrsmith");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        addAcolyteReady();
        harness.addToBattlefield(player2, new Myrsmith());

        // Tap the acolyte
        Permanent acolyte = findPermanent(player1, "Abuna Acolyte");
        acolyte.tap();

        UUID targetId = harness.getPermanentId(player2, "Myrsmith");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void ability2CannotTargetPlayer() {
        addAcolyteReady();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void ability2CannotTargetNoncreatureArtifact() {
        addAcolyteReady();
        Permanent spellbomb = harness.addToBattlefieldAndReturn(player2, new OriginSpellbomb());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, spellbomb.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void ability1CannotTargetNoncreatureArtifact() {
        addAcolyteReady();
        Permanent spellbomb = harness.addToBattlefieldAndReturn(player2, new OriginSpellbomb());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, spellbomb.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void ability1PreventsOnlyOneDamageAcrossSuccessiveSpells() {
        addAcolyteReady();
        harness.setLife(player2, 20);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new GalvanicBlast(), new GalvanicBlast()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void ability2PreventsTwoDamageToOpponentsArtifactCreature() {
        addAcolyteReady();
        Permanent myr = harness.addToBattlefieldAndReturn(player2, new LeadenMyr());
        harness.activateAbility(player1, 0, 1, null, myr.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new GalvanicBlast(), new GalvanicBlast()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, myr.getId());
        harness.assertOnBattlefield(player2, "Leaden Myr");
        assertThat(myr.getDamagePreventionShield()).isZero();

        harness.castAndResolveInstant(player1, 0, myr.getId());
        harness.assertInGraveyard(player2, "Leaden Myr");
    }

    @Test
    void ability1PreventsDamageToPlaneswalker() {
        addAcolyteReady();
        Permanent koth = harness.enterBattlefieldAndReturn(player2, new KothOfTheHammer());
        harness.activateAbility(player1, 0, null, koth.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new GalvanicBlast()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, koth.getId());

        assertThat(koth.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void unusedPlayerShieldExpiresAtEndOfTurn() {
        addAcolyteReady();
        harness.setLife(player2, 20);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.setHand(player2, List.of(new GalvanicBlast()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
