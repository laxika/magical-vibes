package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
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

@CardUsed({ShieldedByFaith.class, GrizzlyBears.class, DoomBlade.class, FountainOfYouth.class})
class ShieldedByFaithTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has indestructible")
    void enchantedCreatureHasIndestructible() {
        Permanent bears = addReadyCreature(player1);
        castShield(player1, bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Accepting the may ability attaches Shielded by Faith to an entering creature you control")
    void acceptingMayAttachesToOwnEnteringCreature() {
        Permanent original = addReadyCreature(player1);
        Permanent shield = addAttachedShield(original);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveCreatureAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        Permanent entering = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != original
                        && permanent.getCard().getName().equals("Grizzly Bears"))
                .findFirst()
                .orElseThrow();
        assertThat(shield.getAttachedTo()).isEqualTo(entering.getId());
    }

    @Test
    @DisplayName("The may ability can attach Shielded by Faith to an opponent's entering creature")
    void acceptingMayAttachesToOpponentsEnteringCreature() {
        Permanent original = addReadyCreature(player1);
        Permanent shield = addAttachedShield(original);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        resolveCreatureAndTrigger();

        harness.handleMayAbilityChosen(player1, true);

        Permanent entering = findPermanent(player2, "Grizzly Bears");
        assertThat(shield.getAttachedTo()).isEqualTo(entering.getId());
    }

    @Test
    @DisplayName("Shielded by Faith cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new ShieldedByFaith()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanted creature survives destruction")
    void enchantedCreatureSurvivesDestruction() {
        Permanent creature = addReadyCreature(player1);
        addAttachedShield(creature);

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the may leaves Shielded by Faith attached to its original creature")
    void staysOnOriginalOnDecline() {
        Permanent original = addReadyCreature(player1);
        Permanent aura = addAttachedShield(original);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(aura.getAttachedTo()).isEqualTo(original.getId());
    }

    private Permanent addReadyCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private Permanent addAttachedShield(Permanent host) {
        Permanent shield = new Permanent(new ShieldedByFaith());
        shield.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player1.getId()).add(shield);
        return shield;
    }

    private void castShield(Player player, Permanent target) {
        harness.setHand(player, List.of(new ShieldedByFaith()));
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player, 0, target.getId());
        harness.passBothPriorities();
    }

    private void resolveCreatureAndTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
