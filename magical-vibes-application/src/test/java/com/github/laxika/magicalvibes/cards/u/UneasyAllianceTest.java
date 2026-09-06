package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({UneasyAlliance.class, GrizzlyBears.class, FountainOfYouth.class})
class UneasyAllianceTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature cannot attack or block")
    void enchantedCreatureCannotAttackOrBlock() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        attachAura(player2, enchanted);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        attachAura(player1, blocker);
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new com.github.laxika.magicalvibes.networking.message.BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker), 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Sacrificing the Aura exiles the enchanted creature and creates a Ninja")
    void sacrificingAuraExilesCreatureAndCreatesNinja() {
        Permanent enchanted = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = attachAura(player1, enchanted);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enchanted);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(enchanted.getCard());
        harness.assertInGraveyard(player1, "Uneasy Alliance");

        Permanent ninja = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(ninja.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(ninja.getCard().getSubtypes()).containsExactly(CardSubtype.NINJA);
        assertThat(ninja.getEffectivePower()).isEqualTo(1);
        assertThat(ninja.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The Aura ability can only be activated as a sorcery")
    void abilityOnlyActivatesAsSorcery() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = attachAura(player1, enchanted);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new UneasyAlliance()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent attachAura(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new UneasyAlliance());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }
}
