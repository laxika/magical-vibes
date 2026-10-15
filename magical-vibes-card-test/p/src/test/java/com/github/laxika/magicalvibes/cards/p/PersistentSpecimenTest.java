package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PersistentSpecimen.class})
@DisplayName("Persistent Specimen")
class PersistentSpecimenTest extends BaseCardTest {

    @Test
    @DisplayName("Activating graveyard ability puts it on the stack")
    void activatingGraveyardAbilityPutsOnStack() {
        PersistentSpecimen specimen = new PersistentSpecimen();
        harness.setGraveyard(player1, List.of(specimen));
        addActivationMana();

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(specimen);
    }

    @Test
    @DisplayName("Resolving graveyard ability returns Persistent Specimen to the battlefield tapped")
    void resolvingGraveyardAbilityReturnsToBattlefieldTapped() {
        PersistentSpecimen specimen = new PersistentSpecimen();
        harness.setGraveyard(player1, List.of(specimen));
        addActivationMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Persistent Specimen");
        assertThat(returned.isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Persistent Specimen");
    }

    @Test
    @DisplayName("Cannot activate graveyard ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        PersistentSpecimen specimen = new PersistentSpecimen();
        harness.setGraveyard(player1, List.of(specimen));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Graveyard ability pays its mana cost")
    void graveyardAbilityPaysManaCost() {
        PersistentSpecimen specimen = new PersistentSpecimen();
        harness.setGraveyard(player1, List.of(specimen));
        addActivationMana();

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    @DisplayName("Can activate graveyard ability again after returning and dying again")
    void canActivateGraveyardAbilityMultipleTimes() {
        PersistentSpecimen specimen = new PersistentSpecimen();
        harness.setGraveyard(player1, List.of(specimen));
        addActivationMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Persistent Specimen");

        Permanent permanent = findPermanent(player1, "Persistent Specimen");
        gd.playerBattlefields.get(player1.getId()).remove(permanent);
        gd.playerGraveyards.get(player1.getId()).add(permanent.getCard());

        addActivationMana();
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Persistent Specimen");
    }

    @Test
    @DisplayName("Only the activated specimen returns, leaving other copies in both graveyards")
    void returnsOnlyTheSourceCard() {
        PersistentSpecimen first = new PersistentSpecimen();
        PersistentSpecimen source = new PersistentSpecimen();
        PersistentSpecimen opponentsCopy = new PersistentSpecimen();
        harness.setGraveyard(player1, List.of(first, source));
        harness.setGraveyard(player2, List.of(opponentsCopy));
        addActivationMana();

        harness.activateGraveyardAbility(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsCopy);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent returned = findPermanent(player1, "Persistent Specimen");
        assertThat(returned.getCard()).isSameAs(source);
        assertThat(returned.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Persistent Specimen");
    }

    @Test
    @DisplayName("Three generic mana cannot pay the black requirement")
    void cannotActivateWithoutBlackMana() {
        harness.setGraveyard(player1, List.of(new PersistentSpecimen()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Persistent Specimen");
    }

    @Test
    @DisplayName("Two activations return the specimen only once")
    void multipleStackedActivationsReturnOnlyOnce() {
        PersistentSpecimen specimen = new PersistentSpecimen();
        harness.setGraveyard(player1, List.of(specimen));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(findPermanent(player1, "Persistent Specimen").getCard()).isSameAs(specimen);
        assertThat(findPermanent(player1, "Persistent Specimen").isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Persistent Specimen");
    }

    @Test
    @DisplayName("An older activation cannot return a specimen that returned and died again")
    void olderActivationCannotReturnANewGraveyardObject() {
        PersistentSpecimen specimen = new PersistentSpecimen();
        harness.setGraveyard(player1, List.of(specimen));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        findPermanent(player1, "Persistent Specimen").setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Persistent Specimen");
        harness.assertNotOnBattlefield(player1, "Persistent Specimen");

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Persistent Specimen");
        harness.assertNotOnBattlefield(player1, "Persistent Specimen");
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
