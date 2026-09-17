package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunfireBalm.class, Shock.class, GlorySeeker.class})
class SunfireBalmTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the next four damage to the target player")
    void preventsNextFourDamageToPlayer() {
        harness.setHand(player1, List.of(new SunfireBalm(), new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 3);

        int lifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Prevents the next four damage to a target creature")
    void preventsNextFourDamageToCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        harness.setHand(player1, List.of(new SunfireBalm(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(creature.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("Cycling prevention can target a creature")
    void cyclingAcceptsPreventionForCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        harness.setHand(player1, List.of(new SunfireBalm(), new Shock()));
        harness.setLibrary(player1, List.of(new GlorySeeker()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        assertThat(creature.getDamagePreventionShield()).isZero();
        harness.assertInGraveyard(player1, "Sunfire Balm");
        harness.assertInHand(player1, "Glory Seeker");
    }

    @Test
    @DisplayName("The prevention shield expires at the end of the turn")
    void preventionShieldExpiresAtEndOfTurn() {
        harness.setHand(player1, List.of(new SunfireBalm()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Cycling and accepting the optional prevention draws a card")
    void cyclingAcceptsPreventionAndDraws() {
        harness.setHand(player1, List.of(new SunfireBalm(), new Shock()));
        harness.setLibrary(player1, List.of(new GlorySeeker()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        int lifeBefore = gd.getLife(player2.getId());

        harness.activateHandAbility(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
        harness.assertInGraveyard(player1, "Sunfire Balm");
        harness.assertInHand(player1, "Glory Seeker");
    }

    @Test
    @DisplayName("Declining the cycling prevention still draws a card")
    void cyclingDeclinesPreventionAndDraws() {
        harness.setHand(player1, List.of(new SunfireBalm(), new Shock()));
        harness.setLibrary(player1, List.of(new GlorySeeker()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        int lifeBefore = gd.getLife(player2.getId());

        harness.activateHandAbility(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        harness.assertInGraveyard(player1, "Sunfire Balm");
        harness.assertInHand(player1, "Glory Seeker");
    }
}
