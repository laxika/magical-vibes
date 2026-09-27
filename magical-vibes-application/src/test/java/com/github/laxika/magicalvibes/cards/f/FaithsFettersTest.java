package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BloodletterQuill;
import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FaithsFetters.class, BorosRecruit.class, BloodletterQuill.class, Forest.class})
class FaithsFettersTest extends BaseCardTest {

    @Test
    void entersAttachedAndGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BloodletterQuill());
        harness.setHand(player1, List.of(new FaithsFetters()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.isAttached() && p.getAttachedTo().equals(target.getId()));
    }

    @Test
    void enchantedCreatureCannotAttack() {
        Permanent creature = addCreatureReady(player1, new BorosRecruit());
        attachAura(creature, player2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void enchantedCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new BorosRecruit());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BorosRecruit());
        attachAura(blocker, player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    void enchantedPermanentCannotActivateNonManaAbilities() {
        Permanent quill = harness.addToBattlefieldAndReturn(player1, new BloodletterQuill());
        attachAura(quill, player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(quill), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    void enchantedLandCanStillActivateManaAbility() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        attachAura(forest, player2);

        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(forest));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    private void attachAura(Permanent target, Player controller) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new FaithsFetters());
        aura.setAttachedTo(target.getId());
    }
}
