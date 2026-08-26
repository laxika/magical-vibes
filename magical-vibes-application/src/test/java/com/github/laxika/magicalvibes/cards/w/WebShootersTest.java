package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WebShooters.class, GrizzlyBears.class})
class WebShootersTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1 and reach")
    void equippedCreatureGetsBoostAndReach() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shooters = addWebShootersReady(player1);
        shooters.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Equip ability attaches Web-Shooters to a creature")
    void equipAbilityAttachesToCreature() {
        Permanent shooters = addWebShootersReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(shooters.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature's attack taps a target creature an opponent controls")
    void attackTriggerTapsOpponentCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shooters = addWebShootersReady(player1);
        shooters.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attack trigger only allows creatures controlled by an opponent")
    void attackTriggerRestrictsTargets() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shooters = addWebShootersReady(player1);
        shooters.setAttachedTo(creature.getId());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(opponentCreature.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Unequipped Web-Shooters do not trigger when a creature attacks")
    void noTriggerWhenUnequipped() {
        addCreatureReady(player1, new GrizzlyBears());
        addWebShootersReady(player1);
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers();

        assertThat(gd.interaction.activeInteraction())
                .isNotInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() instanceof WebShooters);
    }

    private Permanent addWebShootersReady(Player player) {
        Permanent perm = new Permanent(new WebShooters());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void declareAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), null);
    }
}
