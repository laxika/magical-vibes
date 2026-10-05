package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AlpineWatchdog;
import com.github.laxika.magicalvibes.cards.f.Frogify;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PursuedWhale.class, AlpineWatchdog.class, Shock.class, Frogify.class})
class PursuedWhaleTest extends BaseCardTest {

    @Test
    @DisplayName("When Pursued Whale enters, each opponent creates a 1/1 red Pirate token")
    void eachOpponentCreatesPirateToken() {
        castAndResolveWhale();

        List<Permanent> pirates = findPermanents(player2, "Pirate");
        assertThat(pirates).hasSize(1);
        assertThat(findPermanents(player1, "Pirate")).isEmpty();
        assertThat(pirates.getFirst().getCard().getPower()).isEqualTo(1);
        assertThat(pirates.getFirst().getCard().getToughness()).isEqualTo(1);
        assertThat(pirates.getFirst().getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(pirates.getFirst().getCard().getSubtypes()).contains(CardSubtype.PIRATE);
        assertThat(bls.canBlock(gd, pirates.getFirst())).isFalse();
    }

    @Test
    @DisplayName("Pirate tokens force all creatures their controller controls to attack")
    void pirateTokenForcesControllerCreaturesToAttack() {
        castAndResolveWhale();
        Permanent pirate = findPermanent(player2, "Pirate");
        pirate.setSummoningSick(false);
        Permanent watchdog = addCreatureReady(player2, new AlpineWatchdog());

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
        assertThat(watchdog.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("An opponent's spell targeting Pursued Whale costs {3} more")
    void opponentSpellTargetingWhaleCostsMore() {
        Permanent whale = addCreatureReady(player1, new PursuedWhale());
        harness.forceActivePlayer(player2);
        harness.forceStep(gd.currentStep);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, whale.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay targeting tax");
    }

    @Test
    @DisplayName("An opponent can target the Whale by paying exactly three additional mana")
    void opponentCanPayTargetingSurcharge() {
        Permanent whale = addCreatureReady(player1, new PursuedWhale());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player2, 0, whale.getId());
        harness.passBothPriorities();

        assertThat(whale.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("The Whale's controller pays no surcharge for targeting it")
    void controllerSpellIsNotTaxed() {
        Permanent whale = addCreatureReady(player1, new PursuedWhale());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, whale.getId());
        harness.passBothPriorities();

        assertThat(whale.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's spell targeting another creature is not taxed")
    void otherCreatureIsNotProtected() {
        addCreatureReady(player1, new PursuedWhale());
        Permanent watchdog = addCreatureReady(player1, new AlpineWatchdog());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, watchdog.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(watchdog);
        assertThat(findPermanents(player1, "Pursued Whale")).hasSize(1);
    }

    @Test
    @DisplayName("A summoning-sick Pirate still requires its controller's ready creatures to attack")
    void summoningSickPirateRequiresOtherCreaturesToAttack() {
        castAndResolveWhale();
        addCreatureReady(player2, new AlpineWatchdog());

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Tapped and summoning-sick creatures are not required to attack")
    void creaturesUnableToAttackMayStayBack() {
        castAndResolveWhale();
        Permanent watchdog = addCreatureReady(player2, new AlpineWatchdog());
        watchdog.setTapped(true);

        declareAttackers(player2, List.of());

        assertThat(watchdog.isAttacking()).isFalse();
        assertThat(findPermanent(player2, "Pirate").isAttacking()).isFalse();
    }

    @Test
    @DisplayName("The Pirate's attack requirement remains after the Whale leaves")
    void attackRequirementBelongsToPirate() {
        castAndResolveWhale();
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Pursued Whale"));
        addCreatureReady(player2, new AlpineWatchdog());

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("The Whale's targeting surcharge disappears when it loses all abilities")
    void abilityLossRemovesTargetingSurcharge() {
        Permanent whale = addCreatureReady(player1, new PursuedWhale());
        castFrogify(whale);
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, whale.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(whale);
    }

    @Test
    @DisplayName("A Pirate that loses all abilities no longer requires creatures to attack")
    void abilityLossRemovesPirateAttackRequirement() {
        castAndResolveWhale();
        Permanent pirate = findPermanent(player2, "Pirate");
        castFrogify(pirate);
        Permanent watchdog = addCreatureReady(player2, new AlpineWatchdog());

        declareAttackers(player2, List.of());

        assertThat(watchdog.isAttacking()).isFalse();
        assertThat(pirate.isAttacking()).isFalse();
    }

    private void castFrogify(Permanent target) {
        harness.setHand(player1, List.of(new Frogify()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void castAndResolveWhale() {
        harness.setHand(player1, List.of(new PursuedWhale()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
