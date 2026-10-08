package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MachineOverMatter;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({CityscapeLeveler.class, GrizzlyBears.class, Forest.class, MachineOverMatter.class})
class CityscapeLevelerTest extends BaseCardTest {

    @Test
    @DisplayName("Cast trigger destroys a nonland permanent and gives its controller a tapped Powerstone")
    void castTriggerDestroysNonlandPermanentAndCreatesPowerstone() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CityscapeLeveler()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanents(player2, "Powerstone")).singleElement().satisfies(powerstone -> {
            assertThat(powerstone.getCard().getType()).isEqualTo(CardType.ARTIFACT);
            assertThat(powerstone.getCard().getSubtypes()).containsExactly(CardSubtype.POWERSTONE);
            assertThat(powerstone.isTapped()).isTrue();
        });
    }

    @Test
    @DisplayName("Attack trigger destroys a nonland permanent and gives its controller a tapped Powerstone")
    void attackTriggerDestroysNonlandPermanentAndCreatesPowerstone() {
        Permanent leveler = addCreatureReady(player1, new CityscapeLeveler());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(leveler)));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanents(player2, "Powerstone")).hasSize(1);
    }

    @Test
    @DisplayName("Cast trigger can resolve without choosing a target")
    void castTriggerCanResolveWithoutTarget() {
        harness.setHand(player1, List.of(new CityscapeLeveler()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cityscape Leveler");
        assertThat(findPermanents(player1, "Powerstone")).isEmpty();
    }

    @Test
    @DisplayName("Cast trigger cannot target a land")
    void castTriggerCannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new CityscapeLeveler()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    @DisplayName("Unearth returns Cityscape Leveler with haste and exiles it at the next end step")
    void unearthReturnsWithHasteAndExilesAtEndStep() {
        harness.setGraveyard(player1, List.of(new CityscapeLeveler()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent leveler = findPermanent(player1, "Cityscape Leveler");
        assertThat(gqs.hasKeyword(gd, leveler, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Cityscape Leveler");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(cardInExile -> cardInExile.getName().equals("Cityscape Leveler"));
    }

    @Test
    void castTriggerCanDeclineWithLegalTargetAvailable() {
        harness.addToBattlefield(player2, new CityscapeLeveler());
        harness.setHand(player1, List.of(new CityscapeLeveler()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Cityscape Leveler");
        harness.assertOnBattlefield(player2, "Cityscape Leveler");
        assertThat(findPermanents(player1, "Powerstone")).isEmpty();
        assertThat(findPermanents(player2, "Powerstone")).isEmpty();
    }

    @Test
    void attackTriggerCanDeclineWithLegalTargetAvailable() {
        addCreatureReady(player1, new CityscapeLeveler());
        harness.addToBattlefield(player2, new CityscapeLeveler());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Cityscape Leveler");
        assertThat(findPermanents(player1, "Powerstone")).isEmpty();
        assertThat(findPermanents(player2, "Powerstone")).isEmpty();
    }

    @Test
    void indestructibleTargetStillGetsTappedPowerstone() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CityscapeLeveler());
        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.setHand(player1, List.of(new CityscapeLeveler()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Cityscape Leveler");
        assertThat(findPermanents(player2, "Powerstone")).singleElement()
                .satisfies(powerstone -> assertThat(powerstone.isTapped()).isTrue());
        assertThat(findPermanents(player1, "Powerstone")).isEmpty();
    }

    @Test
    void attackTriggerCanDestroyItsOwnSourceAndCreatePowerstoneForItsController() {
        Permanent leveler = addCreatureReady(player1, new CityscapeLeveler());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, leveler.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Cityscape Leveler");
        harness.assertInGraveyard(player1, "Cityscape Leveler");
        assertThat(findPermanents(player1, "Powerstone")).singleElement()
                .satisfies(powerstone -> assertThat(powerstone.isTapped()).isTrue());
    }

    @Test
    void unearthDoesNotTriggerCastAbilityButAllowsImmediateAttackTrigger() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CityscapeLeveler());
        harness.setGraveyard(player1, List.of(new CityscapeLeveler()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Cityscape Leveler");
        harness.assertOnBattlefield(player2, "Cityscape Leveler");
        assertThat(findPermanents(player2, "Powerstone")).isEmpty();

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Cityscape Leveler");
        assertThat(findPermanents(player2, "Powerstone")).singleElement()
                .satisfies(powerstone -> assertThat(powerstone.isTapped()).isTrue());
    }

    @Test
    void bouncingUnearthedLevelerExilesItInsteadOfReturningItToHand() {
        harness.setGraveyard(player1, List.of(new CityscapeLeveler()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();
        Permanent leveler = findPermanent(player1, "Cityscape Leveler");
        harness.setHand(player1, List.of(new MachineOverMatter()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, leveler.getId());

        harness.assertNotOnBattlefield(player1, "Cityscape Leveler");
        harness.assertNotInGraveyard(player1, "Cityscape Leveler");
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Cityscape Leveler"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Cityscape Leveler"));
    }

    @Test
    void targetLeavingBeforeResolutionPreventsPowerstoneCreation() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CityscapeLeveler());
        harness.setHand(player1, List.of(new CityscapeLeveler()));
        harness.setHand(player2, List.of(new MachineOverMatter()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Cityscape Leveler");
        harness.assertInHand(player2, "Cityscape Leveler");
        assertThat(findPermanents(player1, "Powerstone")).isEmpty();
        assertThat(findPermanents(player2, "Powerstone")).isEmpty();
    }

    @Test
    void unearthCannotBeActivatedDuringCombat() {
        harness.setGraveyard(player1, List.of(new CityscapeLeveler()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Cityscape Leveler");
        harness.assertNotOnBattlefield(player1, "Cityscape Leveler");
    }
}
