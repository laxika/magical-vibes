package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CoralhelmGuide;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AdverseConditions.class, Island.class, CoralhelmGuide.class})
class AdverseConditionsTest extends BaseCardTest {

    @Test
    @DisplayName("Taps up to two target creatures, locks their next untap, and creates an Eldrazi Scion")
    void tapsAndLocksUpToTwoCreaturesAndCreatesScion() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new CoralhelmGuide());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new CoralhelmGuide());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new CoralhelmGuide());
        harness.setHand(player1, List.of(new AdverseConditions()));
        addMana();

        harness.castAndResolveInstant(player1, 0, List.of(first.getId(), second.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(first.getSkipUntapCount()).isEqualTo(1);
        assertThat(second.getSkipUntapCount()).isEqualTo(1);
        assertThat(third.isTapped()).isFalse();
        assertThat(third.getSkipUntapCount()).isZero();
        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(1);
    }

    @Test
    @DisplayName("Can resolve with no creature targets")
    void canResolveWithNoTargets() {
        harness.setHand(player1, List.of(new AdverseConditions()));
        addMana();

        harness.castAndResolveInstant(player1, 0, List.of());

        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(1);
    }

    @Test
    @DisplayName("An Eldrazi Scion can be sacrificed for colorless mana")
    void scionCanBeSacrificedForColorlessMana() {
        harness.setHand(player1, List.of(new AdverseConditions()));
        addMana();
        harness.castAndResolveInstant(player1, 0, List.of());

        Permanent scion = findPermanents(player1, "Eldrazi Scion").getFirst();
        int scionIndex = gd.playerBattlefields.get(player1.getId()).indexOf(scion);
        harness.activateAbility(player1, scionIndex, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new AdverseConditions()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void createsAnUntappedColorlessOneOneEldraziScion() {
        harness.setHand(player1, List.of(new AdverseConditions()));
        addMana();

        harness.castAndResolveInstant(player1, 0, List.of());

        List<Permanent> scions = findPermanents(player1, "Eldrazi Scion");
        assertThat(scions).hasSize(1);
        Permanent scion = scions.getFirst();
        assertThat(scion.isTapped()).isFalse();
        assertThat(scion.getCard().isToken()).isTrue();
        assertThat(scion.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(scion.getCard().getColors()).isEmpty();
        assertThat(scion.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.ELDRAZI, CardSubtype.SCION);
        assertThat(gqs.getEffectivePower(gd, scion)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, scion)).isEqualTo(1);
    }

    @Test
    void alreadyTappedCreatureSkipsOnlyItsNextUntap() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CoralhelmGuide());
        target.setTapped(true);
        harness.setHand(player1, List.of(new AdverseConditions()));
        addMana();

        harness.castAndResolveInstant(player1, 0, List.of(target.getId()));

        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(1);
        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void targetsWithDifferentControllersSkipTheirOwnNextUntap() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new CoralhelmGuide());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new CoralhelmGuide());
        harness.setHand(player1, List.of(new AdverseConditions()));
        addMana();

        harness.castAndResolveInstant(player1, 0, List.of(own.getId(), opposing.getId()));

        harness.performUntapStep(player1);
        assertThat(own.isTapped()).isTrue();
        assertThat(opposing.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(opposing.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(own.isTapped()).isFalse();
        assertThat(opposing.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(opposing.isTapped()).isFalse();
    }

    @Test
    void overlappingRestrictionsDoNotSkipTwoUntapSteps() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CoralhelmGuide());
        harness.setHand(player1, List.of(new AdverseConditions(), new AdverseConditions()));
        addMana();
        addMana();

        harness.castAndResolveInstant(player1, 0, List.of(target.getId()));
        harness.castAndResolveInstant(player1, 0, List.of(target.getId()));

        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(2);
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void remainingLegalTargetIsAffectedAndScionIsCreated() {
        Permanent removed = harness.addToBattlefieldAndReturn(player2, new CoralhelmGuide());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new CoralhelmGuide());
        harness.setHand(player1, List.of(new AdverseConditions()));
        addMana();
        harness.castInstant(player1, 0, List.of(removed.getId(), remaining.getId()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, removed));
        harness.passBothPriorities();

        assertThat(remaining.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(1);
        harness.performUntapStep(player2);
        assertThat(remaining.isTapped()).isTrue();
    }

    @Test
    void noScionIsCreatedWhenAllChosenTargetsLeave() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new CoralhelmGuide());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new CoralhelmGuide());
        harness.setHand(player1, List.of(new AdverseConditions()));
        addMana();
        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));

        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToHand(gd, first);
            harness.getPermanentRemovalService().removePermanentToHand(gd, second);
        });
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
        harness.assertInGraveyard(player1, "Adverse Conditions");
    }

    @Test
    void cannotChooseMoreThanTwoTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new CoralhelmGuide());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new CoralhelmGuide());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new CoralhelmGuide());
        harness.setHand(player1, List.of(new AdverseConditions()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotChooseTheSameCreatureTwice() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CoralhelmGuide());
        harness.setHand(player1, List.of(new AdverseConditions()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
