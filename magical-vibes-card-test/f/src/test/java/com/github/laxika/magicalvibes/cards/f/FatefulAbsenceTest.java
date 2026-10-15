package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.ErdwalIlluminator;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FatefulAbsence.class, GarrukWildspeaker.class, GrizzlyBears.class, Plains.class})
class FatefulAbsenceTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature and gives its controller a Clue")
    void destroysCreatureAndItsControllerInvestigates() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castFatefulAbsence(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player2, "Clue")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Destroys a planeswalker and gives its controller a Clue")
    void destroysPlaneswalkerAndItsControllerInvestigates() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        target.setCounterCount(CounterType.LOYALTY, 3);
        castFatefulAbsence(target);

        harness.assertNotOnBattlefield(player2, "Garruk Wildspeaker");
        harness.assertInGraveyard(player2, "Garruk Wildspeaker");
        assertThat(findPermanents(player2, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Rejects a land target")
    void rejectsLandTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new FatefulAbsence()));
        addFatefulAbsenceMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker");
    }

    @Test
    @DisplayName("Does not investigate when the target leaves before resolution")
    void doesNotInvestigateWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FatefulAbsence()));
        addFatefulAbsenceMana();

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Can destroy your own creature and you investigate")
    void canDestroyOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castFatefulAbsence(target);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("An indestructible target survives and its controller still investigates")
    void investigatesEvenWhenTargetIsIndestructible() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.getPersistentGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        castFatefulAbsence(target);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player2, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Regeneration saves the target without preventing investigation")
    void targetCanRegenerateAndItsControllerInvestigates() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setRegenerationShield(1);

        castFatefulAbsence(target);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getRegenerationShield()).isZero();
        assertThat(findPermanents(player2, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("A target gaining hexproof makes the spell fail without investigation")
    void doesNotInvestigateWhenTargetGainsHexproof() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FatefulAbsence()));
        addFatefulAbsenceMana();
        harness.castInstant(player1, 0, target.getId());
        target.getPersistentGrantedKeywords().add(Keyword.HEXPROOF);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanents(player2, "Clue")).isEmpty();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @CardUsed({ErdwalIlluminator.class})
    @DisplayName("Investigation triggers the target controller's Illuminator")
    void investigationTriggersForTargetController() {
        harness.addToBattlefield(player2, new ErdwalIlluminator());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castFatefulAbsence(target);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player2, "Clue")).hasSize(2);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("The created Clue can be sacrificed for two mana to draw a card")
    void clueCanBeSacrificedToDraw() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Plains()));
        castFatefulAbsence(target);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, null);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Plains");
    }

    private void castFatefulAbsence(Permanent target) {
        harness.setHand(player1, List.of(new FatefulAbsence()));
        addFatefulAbsenceMana();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void addFatefulAbsenceMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
