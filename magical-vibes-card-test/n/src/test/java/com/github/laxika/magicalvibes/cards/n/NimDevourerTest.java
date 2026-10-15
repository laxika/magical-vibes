package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FangrenHunter;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
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

@CardUsed({NimDevourer.class, FangrenHunter.class, Ornithopter.class})
class NimDevourerTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 for each artifact controlled")
    void getsPowerForControlledArtifacts() {
        Permanent nim = harness.addToBattlefieldAndReturn(player1, new NimDevourer());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());

        assertThat(gqs.getEffectivePower(gd, nim)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, nim)).isEqualTo(1);
    }

    @Test
    @DisplayName("Returns from the graveyard and then sacrifices a chosen creature during upkeep")
    void returnsAndSacrificesCreatureDuringUpkeep() {
        NimDevourer nim = new NimDevourer();
        harness.setGraveyard(player1, List.of(nim));
        Permanent hunter = harness.addToBattlefieldAndReturn(player1, new FangrenHunter());
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(hunter.getId()));

        harness.assertOnBattlefield(player1, "Nim Devourer");
        harness.assertInGraveyard(player1, "Fangren Hunter");
    }

    @Test
    @DisplayName("Can only activate the graveyard ability during upkeep")
    void canOnlyActivateDuringUpkeep() {
        harness.setGraveyard(player1, List.of(new NimDevourer()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @DisplayName("Can sacrifice the returned Nim Devourer when it is the only creature its controller has")
    void canSacrificeReturnedSelfWhenNoOtherCreature() {
        NimDevourer nim = new NimDevourer();
        harness.setGraveyard(player1, List.of(nim));
        harness.addToBattlefield(player2, new FangrenHunter());
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nim Devourer");
        harness.assertInGraveyard(player1, "Nim Devourer");
        harness.assertOnBattlefield(player2, "Fangren Hunter");
    }

    @Test
    @DisplayName("Cannot activate with only one black mana")
    void requiresTwoBlackMana() {
        harness.setGraveyard(player1, List.of(new NimDevourer()));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate during an opponent's upkeep")
    void cannotActivateDuringOpponentsUpkeep() {
        harness.setGraveyard(player1, List.of(new NimDevourer()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @DisplayName("An older activation cannot return Nim Devourer after it returns and dies again")
    void olderActivationCannotReturnNewGraveyardObject() {
        harness.setGraveyard(player1, List.of(new NimDevourer()));
        harness.addToBattlefield(player1, new FangrenHunter());
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateGraveyardAbility(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1,
                List.of(harness.getPermanentId(player1, "Nim Devourer")));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nim Devourer");
        harness.assertInGraveyard(player1, "Nim Devourer");
        harness.assertNotOnBattlefield(player1, "Fangren Hunter");
        harness.assertInGraveyard(player1, "Fangren Hunter");
    }

    @Test
    @DisplayName("Returning one Nim Devourer does not return another copy in the graveyard")
    void returnsOnlySourceCopy() {
        NimDevourer source = new NimDevourer();
        NimDevourer other = new NimDevourer();
        harness.setGraveyard(player1, List.of(source, other));
        Permanent hunter = harness.addToBattlefieldAndReturn(player1, new FangrenHunter());
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(hunter.getId()));

        assertThat(harness.getPermanentId(player1, "Nim Devourer")).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getId().equals(source.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(other).doesNotContain(source);
    }

    @Test
    @DisplayName("Power bonus updates when an artifact creature is sacrificed after returning")
    void powerBonusUpdatesAfterArtifactSacrifice() {
        harness.setGraveyard(player1, List.of(new NimDevourer()));
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(harness.getPermanentId(player1, "Nim Devourer")))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(5);

        harness.handleMultiplePermanentsChosen(player1, List.of(artifact.getId()));

        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Ornithopter");
    }
}
