package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AbyssalNocturnus;
import com.github.laxika.magicalvibes.cards.m.Mortify;
import com.github.laxika.magicalvibes.cards.s.SkyriderTrainee;
import com.github.laxika.magicalvibes.cards.w.WildCantor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeizeTheSoul.class, WildCantor.class, Mortify.class, AbyssalNocturnus.class, SkyriderTrainee.class})
class SeizeTheSoulTest extends BaseCardTest {

    @Test
    void destroysTargetCreatesSpiritAndHauntTriggersOnHauntedCreatureDeath() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new WildCantor());
        Permanent hauntedCreature = harness.addToBattlefieldAndReturn(player2, new WildCantor());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new WildCantor());

        harness.setHand(player1, List.of(new SeizeTheSoul()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, firstTarget.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(firstTarget.getId()));
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, hauntedCreature.getId());
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Seize the Soul"));

        destroyWithMortify(hauntedCreature);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, secondTarget.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(secondTarget.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Seize the Soul"));
    }

    @Test
    void cannotTargetWhiteOrBlackCreature() {
        Permanent whiteCreature = harness.addToBattlefieldAndReturn(player2, new SkyriderTrainee());
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player2, new AbyssalNocturnus());
        harness.setHand(player1, List.of(new SeizeTheSoul()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, whiteCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, blackCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void hauntTriggerCannotTargetWhiteOrBlackCreature() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new WildCantor());
        Permanent hauntedCreature = harness.addToBattlefieldAndReturn(player2, new WildCantor());
        Permanent whiteCreature = harness.addToBattlefieldAndReturn(player2, new SkyriderTrainee());
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player2, new AbyssalNocturnus());
        Permanent legalTarget = harness.addToBattlefieldAndReturn(player2, new WildCantor());

        harness.setHand(player1, List.of(new SeizeTheSoul()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, firstTarget.getId());
        harness.handlePermanentChosen(player1, hauntedCreature.getId());
        harness.passBothPriorities();

        destroyWithMortify(hauntedCreature);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, whiteCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, blackCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, legalTarget.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    void hauntedCreatureDeathWithoutLegalTargetDoesNotCreateSpirit() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new WildCantor());
        Permanent hauntedCreature = harness.addToBattlefieldAndReturn(player2, new WildCantor());
        harness.setHand(player1, List.of(new SeizeTheSoul()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, firstTarget.getId());
        harness.handlePermanentChosen(player1, hauntedCreature.getId());
        harness.passBothPriorities();

        destroyWithMortify(hauntedCreature);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void hauntedCreatureDeathWithTargetRemovedBeforeResolutionDoesNotCreateSpirit() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new WildCantor());
        Permanent hauntedCreature = harness.addToBattlefieldAndReturn(player2, new WildCantor());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new WildCantor());
        harness.setHand(player1, List.of(new SeizeTheSoul()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, firstTarget.getId());
        harness.handlePermanentChosen(player1, hauntedCreature.getId());
        harness.passBothPriorities();

        destroyWithMortify(hauntedCreature);
        harness.handlePermanentChosen(player1, secondTarget.getId());
        destroyWithMortify(secondTarget);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
    }

    @Test
    void spellWithTargetRemovedBeforeResolutionDoesNotCreateSpiritOrHaunt() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WildCantor());
        harness.addToBattlefield(player2, new WildCantor());
        harness.setHand(player1, List.of(new SeizeTheSoul()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, target.getId());

        destroyWithMortify(target);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Spirit")).isZero();
        harness.assertInGraveyard(player1, "Seize the Soul");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canHauntTheWhiteSpiritCreatedByTheSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WildCantor());
        harness.setHand(player1, List.of(new SeizeTheSoul()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Spirit"));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Seize the Soul"));
        harness.assertNotInGraveyard(player1, "Seize the Soul");
    }

    private void destroyWithMortify(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Mortify()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
    }
}
