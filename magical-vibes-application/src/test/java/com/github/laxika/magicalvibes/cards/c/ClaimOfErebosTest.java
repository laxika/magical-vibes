package com.github.laxika.magicalvibes.cards.c;

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

@CardUsed({ClaimOfErebos.class, CyclopsOfOneEyedPass.class})
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
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new CyclopsOfOneEyedPass());
        harness.addMana(player1, ManaColor.BLACK, 2);
        readyMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, otherCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a player");
    }

    @Test
    @DisplayName("The granted ability is lost when Claim of Erebos leaves the battlefield")
    void abilityIsLostWhenAuraLeaves() {
        Permanent creature = addReadyCreature();
        Permanent aura = addAttachedClaim(creature);

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        harness.addMana(player1, ManaColor.BLACK, 2);
        readyMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void summoningSickCreatureCannotActivateGrantedTapAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CyclopsOfOneEyedPass());
        creature.setSummoningSick(true);
        addAttachedClaim(creature);
        harness.addMana(player1, ManaColor.BLACK, 2);
        readyMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(gd.stack).isEmpty();
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void tappedCreatureCannotActivateGrantedAbility() {
        Permanent creature = addReadyCreature();
        addAttachedClaim(creature);
        creature.tap();
        harness.addMana(player1, ManaColor.BLACK, 2);
        readyMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityRequiresBlackMana() {
        Permanent creature = addReadyCreature();
        addAttachedClaim(creature);
        harness.addMana(player1, ManaColor.RED, 2);
        readyMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void opponentControlsAbilityGrantedByYourAura() {
        Permanent creature = addCreatureReady(player2, new CyclopsOfOneEyedPass());
        Permanent aura = addAttachedClaim(creature);
        harness.addMana(player2, ManaColor.BLACK, 2);
        readyMainPhase();

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(creature.isTapped()).isTrue();
        assertThat(aura.isTapped()).isFalse();
    }

    @Test
    void activatedAbilityResolvesAfterAuraLeaves() {
        Permanent creature = addReadyCreature();
        Permanent aura = addAttachedClaim(creature);
        harness.addMana(player1, ManaColor.BLACK, 2);
        readyMainPhase();

        harness.activateAbility(player1, 0, null, player2.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private Permanent addReadyCreature() {
        return addCreatureReady(player1, new CyclopsOfOneEyedPass());
    }

    private Permanent addAttachedClaim(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ClaimOfErebos());
        aura.setAttachedTo(creature.getId());
        return aura;
    }

    private void readyMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
