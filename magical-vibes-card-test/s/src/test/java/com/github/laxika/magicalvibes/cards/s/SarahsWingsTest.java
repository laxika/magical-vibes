package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.l.LeylineOfPunishment;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SarahsWings.class, GrizzlyBears.class, FountainOfYouth.class,
        LeylineOfPunishment.class, ProdigalPyromancer.class, Shock.class})
class SarahsWingsTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a target creature flying until end of turn")
    void givesCreatureFlyingUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SarahsWings()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Gives a target player flying, preventing damage from creatures without flying")
    void givesPlayerFlying() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SarahsWings()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        int lifeBeforeCombat = gd.getLife(player2.getId());
        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBeforeCombat);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new SarahsWings()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or player");
    }

    @Test
    void doesNotGrantFlyingToCreatureThatLeftAndReturned() {
        GrizzlyBears card = new GrizzlyBears();
        Permanent original = harness.addToBattlefieldAndReturn(player2, card);
        harness.setHand(player1, List.of(new SarahsWings()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, original.getId());

        gd.playerBattlefields.get(player2.getId()).remove(original);
        Permanent returned = harness.addToBattlefieldAndReturn(player2, card);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, returned, Keyword.FLYING)).isFalse();
        harness.assertInGraveyard(player1, "Sarah's Wings");
    }

    @Test
    void flyingCreatureCanDamageFlyingPlayer() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SarahsWings(), new SarahsWings()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, attacker.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        int lifeBefore = gd.getLife(player2.getId());
        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void playerFlyingExpiresAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SarahsWings()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        attacker.setAttacking(true);
        int lifeBefore = gd.getLife(player2.getId());
        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void preventsNoncombatCreatureDamageToController() {
        addCreatureReady(player2, new ProdigalPyromancer());
        harness.setHand(player1, List.of(new SarahsWings()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void doesNotPreventNoncreatureSpellDamage() {
        harness.setHand(player1, List.of(new SarahsWings(), new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        int lifeBefore = gd.getLife(player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void cannotPreventUnpreventableCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LeylineOfPunishment());
        harness.setHand(player1, List.of(new SarahsWings()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        int lifeBefore = gd.getLife(player2.getId());
        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void cannotPreventUnpreventableNoncombatDamage() {
        addCreatureReady(player1, new ProdigalPyromancer());
        harness.addToBattlefield(player1, new LeylineOfPunishment());
        harness.setHand(player1, List.of(new SarahsWings()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        int lifeBefore = gd.getLife(player2.getId());
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }
}
