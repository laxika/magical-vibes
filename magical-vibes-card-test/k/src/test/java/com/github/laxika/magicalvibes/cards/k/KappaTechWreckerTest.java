package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KappaTechWrecker.class, AuraOfSilence.class, GrizzlyBears.class, Spellbook.class})
class KappaTechWreckerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a deathtouch counter")
    void entersWithDeathtouchCounter() {
        Permanent kappa = harness.enterBattlefieldAndReturn(player1, new KappaTechWrecker());

        assertThat(kappa.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
    }

    @Test
    @DisplayName("Accepting the combat trigger removes the counter and exiles an artifact")
    void acceptsCombatTriggerAndExilesArtifact() {
        Permanent kappa = addAttackingKappa();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(artifact.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        resolveAllTriggers();

        assertThat(kappa.getCounterCount(CounterType.DEATHTOUCH)).isZero();
        harness.assertNotOnBattlefield(player2, "Spellbook");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Spellbook"));
    }

    @Test
    @DisplayName("The combat trigger only offers artifacts or enchantments controlled by the damaged player")
    void onlyOffersValidPermanents() {
        addAttackingKappa();
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent damagedPlayerEnchantment = harness.addToBattlefieldAndReturn(player2, new AuraOfSilence());
        Permanent damagedPlayerCreature = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(damagedPlayerEnchantment.getId())
                .doesNotContain(ownArtifact.getId(), damagedPlayerCreature.getId());
    }

    @Test
    @DisplayName("Declining the combat trigger preserves the deathtouch counter")
    void declineCombatTrigger() {
        Permanent kappa = addAttackingKappa();
        harness.addToBattlefieldAndReturn(player2, new Spellbook());

        resolveCombat();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(kappa.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Spellbook");
    }

    @Test
    @DisplayName("Removing the counter does nothing when the damaged player controls no legal permanent")
    void noLegalTargetAfterRemovingCounter() {
        Permanent kappa = addAttackingKappa();
        addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(kappa.getCounterCount(CounterType.DEATHTOUCH)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Ninjutsu returns the attacker as a cost and enters tapped and attacking with a counter")
    void ninjutsuEntersWithDeathtouchCounter() {
        Permanent attacker = addCreatureReady(player1, new KappaTechWrecker());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        KappaTechWrecker incoming = new KappaTechWrecker();
        harness.setHand(player1, List.of(incoming));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, attacker.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerHands.get(player1.getId())).contains(attacker.getCard(), incoming);

        harness.passBothPriorities();

        Permanent kappa = findPermanent(player1, "Kappa Tech-Wrecker");
        assertThat(kappa.isTapped()).isTrue();
        assertThat(kappa.isAttacking()).isTrue();
        assertThat(kappa.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(kappa.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(incoming);
    }

    @Test
    @DisplayName("Combat damage cannot exile a permanent without a deathtouch counter")
    void cannotExileWithoutCounter() {
        Permanent kappa = addAttackingKappa();
        kappa.setCounterCount(CounterType.DEATHTOUCH, 0);
        harness.addToBattlefield(player2, new Spellbook());

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Spellbook");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Removing one of multiple deathtouch counters still exiles an enchantment")
    void removesOnlyOneCounterAndExilesEnchantment() {
        Permanent kappa = addAttackingKappa();
        kappa.setCounterCount(CounterType.DEATHTOUCH, 2);
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new AuraOfSilence());

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(kappa.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Aura of Silence");
        harness.handlePermanentChosen(player1, enchantment.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Aura of Silence");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(enchantment.getCard());
    }

    @Test
    @DisplayName("Leaving the battlefield before counter removal prevents the exile ability")
    void sourceLeavesBeforeCounterRemoval() {
        Permanent kappa = addAttackingKappa();
        harness.addToBattlefield(player2, new Spellbook());

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, kappa);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Kappa Tech-Wrecker");
        harness.assertOnBattlefield(player2, "Spellbook");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    private Permanent addAttackingKappa() {
        Permanent kappa = harness.enterBattlefieldAndReturn(player1, new KappaTechWrecker());
        kappa.setSummoningSick(false);
        kappa.setAttacking(true);
        return kappa;
    }
}
