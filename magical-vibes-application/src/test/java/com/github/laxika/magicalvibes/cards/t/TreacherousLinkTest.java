package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GhituFireEater;
import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TreacherousLink.class, GiantCockroach.class, GhituFireEater.class, ThranLens.class})
class TreacherousLinkTest extends BaseCardTest {

    @Test
    @DisplayName("Redirects noncombat damage to the enchanted creature's controller")
    void redirectsNoncombatDamageToEnchantedCreatureController() {
        Permanent target = addCreatureReady(player2, new GiantCockroach());
        Permanent fireEater = addCreatureReady(player1, new GhituFireEater());
        castTreacherousLink(target);

        harness.activateAbility(player1, indexOf(player1, fireEater), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Redirects combat damage to the enchanted creature's controller")
    void redirectsCombatDamageToEnchantedCreatureController() {
        Permanent target = addCreatureReady(player2, new GiantCockroach());
        Permanent link = castTreacherousLink(target);
        Permanent attacker = addCreatureReady(player1, new GiantCockroach());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(indexOf(player1, attacker)));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(indexOf(player2, target), indexOf(player1, attacker))));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(link);
    }

    @Test
    @DisplayName("Stops redirecting damage after the Aura becomes unattached")
    void stopsRedirectingWhenUnattached() {
        Permanent target = addCreatureReady(player2, new GiantCockroach());
        Permanent fireEater = addCreatureReady(player1, new GhituFireEater());
        Permanent link = castTreacherousLink(target);
        link.setAttachedTo(null);

        harness.activateAbility(player1, indexOf(player1, fireEater), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ThranLens());
        harness.setHand(player1, List.of(new TreacherousLink()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent castTreacherousLink(Permanent target) {
        harness.setHand(player1, List.of(new TreacherousLink()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        return findPermanent(player1, "Treacherous Link");
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
