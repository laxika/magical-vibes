package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class NaruMehaMasterWizardTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has CopySpellEffect ETB and StaticBoostEffect for Wizards")
    void hasCorrectEffects() {
        NaruMehaMasterWizard card = new NaruMehaMasterWizard();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(CopySpellEffect.class);
        CopySpellEffect copyEffect = (CopySpellEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(copyEffect.spellFilter()).isNotNull();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        StaticBoostEffect boost = (StaticBoostEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(boost.powerBoost()).isEqualTo(1);
        assertThat(boost.toughnessBoost()).isEqualTo(1);
        assertThat(boost.scope()).isEqualTo(GrantScope.OWN_CREATURES);
        assertThat(boost.filter()).isEqualTo(new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.WIZARD)));
    }

    @Test
    @DisplayName("Does not require spell target at cast time (creature)")
    void doesNotRequireSpellTargetAtCastTime() {
        NaruMehaMasterWizard card = new NaruMehaMasterWizard();

        assertThat(EffectResolution.needsSpellTarget(card)).isFalse();
        assertThat(EffectResolution.needsTarget(card)).isFalse();
    }

    // ===== ETB spell copy — targeting =====

    @Test
    @DisplayName("ETB triggers permanent choice to select a spell on the stack")
    void etbTriggersSpellTargetChoice() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        NaruMehaMasterWizard naru = new NaruMehaMasterWizard();
        harness.setHand(player1, List.of(counsel, naru));
        harness.addMana(player1, ManaColor.BLUE, 7);

        // Cast Counsel of the Soratami (sorcery)
        harness.castSorcery(player1, 0, 0);
        // Cast Naru Meha in response (has flash)
        harness.castCreature(player1, 0);
        // Resolve Naru Meha creature spell → enters battlefield → ETB triggers
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Should be awaiting permanent choice (to pick a spell from the stack)
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.permanentChoice().validIds()).contains(counsel.getId());
    }

    @Test
    @DisplayName("ETB does not trigger if no valid spells on the stack")
    void etbDoesNotTriggerWithoutValidSpells() {
        NaruMehaMasterWizard naru = new NaruMehaMasterWizard();
        harness.setHand(player1, List.of(naru));
        harness.addMana(player1, ManaColor.BLUE, 4);

        // Cast Naru Meha with empty stack (no spells to target)
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        GameData gd = harness.getGameData();
        // Should not be awaiting any input
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        // Naru Meha should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Naru Meha, Master Wizard"));
    }

    @Test
    @DisplayName("Cannot target opponent's spell (you control filter)")
    void cannotTargetOpponentSpell() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player2, List.of(counsel));
        harness.addMana(player2, ManaColor.BLUE, 3);

        NaruMehaMasterWizard naru = new NaruMehaMasterWizard();
        harness.setHand(player1, List.of(naru));
        harness.addMana(player1, ManaColor.BLUE, 4);

        // Player 2 casts Counsel
        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, 0);
        // Player 1 casts Naru Meha (flash) in response
        harness.passPriority(player2);
        harness.castCreature(player1, 0);
        // Resolve Naru Meha → ETB
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Opponent's spell should not be a valid target — ETB should skip
        // because the filter requires "you control"
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Cannot target creature spell (instant or sorcery filter)")
    void cannotTargetCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        NaruMehaMasterWizard naru = new NaruMehaMasterWizard();
        harness.setHand(player1, List.of(bears, naru));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 4);

        // Cast Grizzly Bears
        harness.castCreature(player1, 0);
        // Cast Naru Meha in response
        harness.castCreature(player1, 0);
        // Resolve Naru Meha → ETB
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Creature spell should not be a valid target
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    // ===== ETB spell copy — resolution =====

    @Test
    @DisplayName("Selecting a spell target puts ETB trigger on stack and resolving copies the spell")
    void etbCopiesTargetSpell() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        NaruMehaMasterWizard naru = new NaruMehaMasterWizard();
        harness.setHand(player1, List.of(counsel, naru));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castSorcery(player1, 0, 0);
        harness.castCreature(player1, 0);
        // Resolve Naru Meha → ETB trigger
        harness.passBothPriorities();

        // Select the spell target
        harness.handlePermanentChosen(player1, counsel.getId());

        GameData gd = harness.getGameData();
        // Stack should have: original Counsel + ETB trigger (targeting Counsel)
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        // Resolve ETB trigger → copies Counsel
        harness.passBothPriorities();

        // Stack should now have: original Counsel + copy of Counsel
        assertThat(gd.stack).anySatisfy(se ->
                assertThat(se.getDescription()).isEqualTo("Copy of Counsel of the Soratami"));
    }

    @Test
    @DisplayName("Copy of draw spell makes the controller draw cards")
    void copyOfDrawSpellDrawsCards() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        NaruMehaMasterWizard naru = new NaruMehaMasterWizard();
        harness.setHand(player1, List.of(counsel, naru));
        harness.addMana(player1, ManaColor.BLUE, 7);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castSorcery(player1, 0, 0);
        harness.castCreature(player1, 0);
        // Resolve Naru Meha → ETB
        harness.passBothPriorities();
        // Select target spell
        harness.handlePermanentChosen(player1, counsel.getId());
        // Resolve ETB trigger → copy created
        harness.passBothPriorities();
        // Resolve copy → draw 2
        harness.passBothPriorities();
        // Resolve original → draw 2
        harness.passBothPriorities();

        int handAfter = gd.playerHands.get(player1.getId()).size();
        // Cast 2 cards (-2), drew 4 total (+4) = net +2
        assertThat(handAfter - handBefore).isEqualTo(2);
    }

    // ===== Static ability — Wizard lord =====

    @Test
    @DisplayName("Other Wizards get +1/+1")
    void otherWizardsGetBoost() {
        FugitiveWizard wizard = new FugitiveWizard();
        harness.addToBattlefield(player1, wizard);
        NaruMehaMasterWizard naru = new NaruMehaMasterWizard();
        harness.addToBattlefield(player1, naru);

        // FugitiveWizard is a 1/1 Wizard — with Naru Meha's lord effect, should get +1/+1
        Permanent wizardPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fugitive Wizard"))
                .findFirst().orElseThrow();
        var bonus = gqs.computeStaticBonus(gd, wizardPerm);
        assertThat(bonus.power()).isEqualTo(1);
        assertThat(bonus.toughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Naru Meha does not boost itself (other Wizards)")
    void doesNotBoostSelf() {
        NaruMehaMasterWizard naru = new NaruMehaMasterWizard();
        harness.addToBattlefield(player1, naru);

        // Naru Meha is a 3/3 — should not boost itself
        Permanent naruPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Naru Meha, Master Wizard"))
                .findFirst().orElseThrow();
        var bonus = gqs.computeStaticBonus(gd, naruPerm);
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("Non-Wizard creatures are not boosted")
    void nonWizardNotBoosted() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);
        NaruMehaMasterWizard naru = new NaruMehaMasterWizard();
        harness.addToBattlefield(player1, naru);

        // Grizzly Bears is not a Wizard — should get no boost
        Permanent bearsPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        var bonus = gqs.computeStaticBonus(gd, bearsPerm);
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }
}
