package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.Puppeteer;
import com.github.laxika.magicalvibes.cards.r.RhoxBodyguard;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ControlledInstincts.class, FugitiveWizard.class, GrizzlyBears.class,
        HillGiant.class, Pacifism.class, Puppeteer.class, RhoxBodyguard.class})
class ControlledInstinctsTest extends BaseCardTest {

    @Test
    @DisplayName("Can enchant a red creature")
    void canEnchantRedCreature() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.setHand(player1, List.of(new ControlledInstincts()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Controlled Instincts")
                        && p.isAttached()
                        && p.getAttachedTo().equals(giant.getId()));
    }

    @Test
    @DisplayName("Can enchant a green creature")
    void canEnchantGreenCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ControlledInstincts()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Controlled Instincts")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bears.getId()));
    }

    @Test
    @DisplayName("Cannot enchant a creature that is neither red nor green")
    void cannotEnchantOffColorCreature() {
        // A legal green target exists so the card is playable; the blue creature is rejected.
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent wizard = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard());

        harness.setHand(player1, List.of(new ControlledInstincts()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, wizard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a red or green creature");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        // A legal green target exists so the card is playable; the noncreature is rejected.
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        aura.setAttachedTo(bears.getId());

        harness.setHand(player1, List.of(new ControlledInstincts()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, aura.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a red or green creature");
    }

    @Test
    @DisplayName("Enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent giant = addCreatureReady(player2, new HillGiant());
        giant.tap();

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ControlledInstincts());
        aura.setAttachedTo(giant.getId());

        advanceToNextTurn(player1);

        assertThat(giant.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untapped enchanted creature stays untapped (aura does not tap)")
    void untappedCreatureStaysUntapped() {
        Permanent giant = addCreatureReady(player2, new HillGiant());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ControlledInstincts());
        aura.setAttachedTo(giant.getId());

        advanceToNextTurn(player1);

        assertThat(giant.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Creature untaps again after Controlled Instincts is removed")
    void creatureUntapsAfterAuraRemoved() {
        Permanent giant = addCreatureReady(player2, new HillGiant());
        giant.tap();

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ControlledInstincts());
        aura.setAttachedTo(giant.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        advanceToNextTurn(player1);

        assertThat(giant.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Resolving Controlled Instincts does not tap its target")
    void resolvingDoesNotTapCreature() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new ControlledInstincts()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();

        assertThat(giant.isTapped()).isFalse();
        assertThat(findPermanent(player1, "Controlled Instincts").getAttachedTo())
                .isEqualTo(giant.getId());
    }

    @Test
    @DisplayName("An ability can untap the enchanted creature outside the untap step")
    void abilityCanUntapEnchantedCreature() {
        addCreatureReady(player1, new Puppeteer());
        Permanent giant = addCreatureReady(player2, new HillGiant());
        giant.tap();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ControlledInstincts());
        aura.setAttachedTo(giant.getId());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, giant.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(giant.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Controlled Instincts");
        assertThat(aura.getAttachedTo()).isEqualTo(giant.getId());
    }

    @Test
    @DisplayName("Controlled Instincts goes to the graveyard if its target leaves before resolution")
    void targetLeavingBeforeResolutionPreventsAttachment() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new ControlledInstincts()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, giant.getId());

        gd.playerBattlefields.get(player2.getId()).remove(giant);
        gd.playerGraveyards.get(player2.getId()).add(giant.getCard());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Controlled Instincts");
        harness.assertInGraveyard(player1, "Controlled Instincts");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can enchant its controller's green and white creature without locking other creatures")
    void canEnchantOwnMulticoloredCreature() {
        Permanent bodyguard = harness.addToBattlefieldAndReturn(player1, new RhoxBodyguard());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bodyguard.tap();
        bears.tap();
        harness.setHand(player1, List.of(new ControlledInstincts()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, bodyguard.getId());
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Controlled Instincts").getAttachedTo())
                .isEqualTo(bodyguard.getId());

        harness.performUntapStep(player1);

        assertThat(bodyguard.isTapped()).isTrue();
        assertThat(bears.isTapped()).isFalse();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
    }
}
