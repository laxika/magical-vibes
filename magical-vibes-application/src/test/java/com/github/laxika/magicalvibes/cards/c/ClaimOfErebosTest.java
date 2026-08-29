package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimOfErebosTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Claim of Erebos attaches it to the target creature")
    void resolvingAttachesToTargetCreature() {
        Permanent creature = addReadyCreature();
        harness.setHand(player1, List.of(new ClaimOfErebos()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Claim of Erebos")
                        && permanent.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Enchanted creature can make a target player lose 2 life")
    void enchantedCreatureMakesPlayerLoseLife() {
        Permanent creature = addReadyCreature();
        addAttachedClaim(creature);
        harness.addMana(player1, ManaColor.BLACK, 2);
        readyMainPhase();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The granted ability can target its controller")
    void abilityCanTargetController() {
        Permanent creature = addReadyCreature();
        addAttachedClaim(creature);
        harness.addMana(player1, ManaColor.BLACK, 2);
        readyMainPhase();

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The granted ability cannot target a permanent")
    void abilityCannotTargetPermanent() {
        Permanent creature = addReadyCreature();
        addAttachedClaim(creature);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.addMana(player1, ManaColor.BLACK, 2);
        readyMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a player");
    }

    @Test
    @DisplayName("The granted ability is lost when Claim of Erebos leaves the battlefield")
    void abilityIsLostWhenAuraLeaves() {
        Permanent creature = addReadyCreature();
        Permanent aura = addAttachedClaim(creature);

        assertThat(gs.getEffectiveActivatedAbilities(gd, creature)).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gs.getEffectiveActivatedAbilities(gd, creature)).isEmpty();
    }

    private Permanent addReadyCreature() {
        return addCreatureReady(player1, new GrizzlyBears());
    }

    private Permanent addAttachedClaim(Permanent creature) {
        Permanent aura = new Permanent(new ClaimOfErebos());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }

    private void readyMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
