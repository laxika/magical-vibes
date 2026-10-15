package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EldraziDisplacer.class, GrizzlyBears.class, Clone.class})
class EldraziDisplacerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles another creature and returns it tapped under its owner's control")
    void flickersAnotherCreatureUnderItsOwnersControlTapped() {
        harness.addToBattlefield(player1, new EldraziDisplacer());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID oldBearsId = bears.getId();

        harness.activateAbility(player1, 0, null, oldBearsId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        Permanent returned = findPermanent(player2, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(oldBearsId);
        assertThat(returned.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target Eldrazi Displacer itself")
    void cannotTargetItself() {
        harness.addToBattlefield(player1, new EldraziDisplacer());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID displacerId = harness.getPermanentId(player1, "Eldrazi Displacer");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, displacerId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires colorless mana in addition to two generic mana")
    void requiresColorlessMana() {
        harness.addToBattlefield(player1, new EldraziDisplacer());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Displacer can flicker another Displacer using mixed mana")
    void tappedDisplacerCanTargetAnotherDisplacer() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new EldraziDisplacer());
        source.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EldraziDisplacer());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Eldrazi Displacer")).isEqualTo(2);
        Permanent returned = findPermanents(player1, "Eldrazi Displacer").stream()
                .filter(permanent -> !permanent.getId().equals(source.getId()))
                .findFirst().orElseThrow();
        assertThat(returned.getId()).isNotEqualTo(target.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(returned.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("A stolen creature returns to its owner rather than its previous controller")
    void stolenCreatureReturnsToOwner() {
        harness.addToBattlefield(player1, new EldraziDisplacer());
        EldraziDisplacer stolenCard = new EldraziDisplacer();
        stolenCard.setOwnerId(player2.getId());
        Permanent stolen = harness.addToBattlefieldAndReturn(player1, stolenCard);
        gd.stolenCreatures.put(stolen.getId(), player2.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, stolen.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Eldrazi Displacer")).isEqualTo(1);
        Permanent returned = findPermanent(player2, "Eldrazi Displacer");
        assertThat(returned.getId()).isNotEqualTo(stolen.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.stolenCreatures).doesNotContainKey(stolen.getId());
    }

    @Test
    @DisplayName("A second activation does not flicker the new object returned by the first")
    void originalTargetLeavingMakesEarlierActivationFizzle() {
        harness.addToBattlefield(player1, new EldraziDisplacer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EldraziDisplacer());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        Permanent returned = findPermanent(player2, "Eldrazi Displacer");
        assertThat(returned.getId()).isNotEqualTo(target.getId());
        returned.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Eldrazi Displacer").getId()).isEqualTo(returned.getId());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({EldraziDisplacer.class, GrizzlyBears.class, Clone.class})
    @DisplayName("A flickered Clone may choose a new creature to copy and returns tapped")
    void flickeredCloneCanChooseNewCopy() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new EldraziDisplacer());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        Permanent clone = findPermanent(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, clone.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, source.getId());

        Permanent returned = findPermanents(player1, "Eldrazi Displacer").stream()
                .filter(permanent -> !permanent.getId().equals(source.getId()))
                .findFirst().orElseThrow();
        assertThat(returned.getOriginalCard()).isInstanceOf(Clone.class);
        assertThat(returned.getId()).isNotEqualTo(clone.getId());
        assertThat(returned.isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Clone");
    }
}
