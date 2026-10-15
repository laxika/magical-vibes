package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.c.ConeOfFlame;
import com.github.laxika.magicalvibes.cards.c.Commandeer;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NaruMehaMasterWizard.class, CounselOfTheSoratami.class, FugitiveWizard.class,
        GrizzlyBears.class, Shock.class, ConeOfFlame.class, Commandeer.class})
class NaruMehaMasterWizardTest extends BaseCardTest {

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
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds()).contains(counsel.getId());
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
        harness.assertOnBattlefield(player1, "Naru Meha, Master Wizard");
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

    @Test
    @DisplayName("Other Wizards get +1/+1")
    void otherWizardsGetBoost() {
        Permanent wizardPerm = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        NaruMehaMasterWizard naru = new NaruMehaMasterWizard();
        harness.addToBattlefield(player1, naru);

        // FugitiveWizard is a 1/1 Wizard — with Naru Meha's lord effect, should get +1/+1
        var bonus = gqs.computeStaticBonus(gd, wizardPerm);
        assertThat(bonus.power()).isEqualTo(1);
        assertThat(bonus.toughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Naru Meha does not boost itself (other Wizards)")
    void doesNotBoostSelf() {
        Permanent naruPerm = harness.addToBattlefieldAndReturn(player1, new NaruMehaMasterWizard());
        var bonus = gqs.computeStaticBonus(gd, naruPerm);
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("Non-Wizard creatures are not boosted")
    void nonWizardNotBoosted() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        NaruMehaMasterWizard naru = new NaruMehaMasterWizard();
        harness.addToBattlefield(player1, naru);

        // Grizzly Bears is not a Wizard — should get no boost
        var bonus = gqs.computeStaticBonus(gd, bearsPerm);
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    void opponentsWizardsAreNotBoosted() {
        Permanent wizard = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard());
        harness.addToBattlefield(player1, new NaruMehaMasterWizard());

        var bonus = gqs.computeStaticBonus(gd, wizard);
        assertThat(bonus.power()).isZero();
        assertThat(bonus.toughness()).isZero();
    }

    @Test
    void canChooseNewTargetForInstantCopy() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock, new NaruMehaMasterWizard()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, shock.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void canKeepOriginalTargetForInstantCopy() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock, new NaruMehaMasterWizard()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, shock.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        harness.passBothPriorities();
        harness.assertLife(player2, 16);
    }

    @Test
    void canChooseNewTargetsForEveryTargetOfCopiedSpell() {
        Permanent originalFirst = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent originalSecond = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent originalThird = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent newFirst = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent newSecond = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent newThird = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        ConeOfFlame cone = new ConeOfFlame();
        harness.setHand(player1, List.of(cone, new NaruMehaMasterWizard()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0,
                List.of(originalFirst.getId(), originalSecond.getId(), originalThird.getId()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, cone.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, newFirst.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, newSecond.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, newThird.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(originalFirst, originalSecond, originalThird, newFirst)
                .doesNotContain(newSecond, newThird);
        assertThat(newFirst.getMarkedDamage()).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(originalFirst, newFirst)
                .doesNotContain(originalSecond, originalThird);
    }

    @Test
    void doesNotCopySpellWhoseControlChangedBeforeTriggerResolves() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel, new NaruMehaMasterWizard()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.setHand(player2, List.of(new Commandeer()));
        harness.addMana(player2, ManaColor.BLUE, 7);

        harness.castSorcery(player1, 0, 0);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, counsel.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, counsel.getId());
        harness.passBothPriorities();

        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().isCopy()).isFalse();
        assertThat(gd.stack.getFirst().getCard()).isSameAs(counsel);
    }
}
