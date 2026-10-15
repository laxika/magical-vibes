package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BloodBairn;
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

@CardUsed({ScourgeOfNelToth.class, BloodBairn.class, Swamp.class})
class ScourgeOfNelTothTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast from the graveyard by paying black mana and sacrificing two creatures")
    void castsFromGraveyardBySacrificingTwoCreatures() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new BloodBairn());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new BloodBairn());

        ScourgeOfNelToth scourge = new ScourgeOfNelToth();
        harness.setGraveyard(player1, List.of(scourge));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castFromGraveyardWithSacrifices(player1, 0, null,
                List.of(firstCreature.getId(), secondCreature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Scourge of Nel Toth");
        harness.assertInGraveyard(player1, "Blood Bairn");
        harness.assertNotOnBattlefield(player1, "Blood Bairn");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Requires exactly two creatures for the graveyard cast")
    void requiresTwoCreaturesForGraveyardCast() {
        BloodBairn creature = new BloodBairn();
        harness.addToBattlefield(player1, creature);
        ScourgeOfNelToth scourge = new ScourgeOfNelToth();
        harness.setGraveyard(player1, List.of(scourge));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castFromGraveyardWithSacrifices(
                player1, 0, null, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("choose exactly 2 permanents");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Scourge of Nel Toth");
    }

    @Test
    @DisplayName("Cannot sacrifice the same creature twice")
    void cannotSacrificeSameCreatureTwice() {
        BloodBairn creature = new BloodBairn();
        harness.addToBattlefield(player1, creature);
        ScourgeOfNelToth scourge = new ScourgeOfNelToth();
        harness.setGraveyard(player1, List.of(scourge));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castFromGraveyardWithSacrifices(
                player1, 0, null, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate permanents");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Blood Bairn");
    }

    @Test
    void sacrificesArePaidBeforeTheSpellResolves() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ScourgeOfNelToth());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ScourgeOfNelToth());
        ScourgeOfNelToth spell = new ScourgeOfNelToth();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castFromGraveyardWithSacrifices(player1, 0, null, List.of(first.getId(), second.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(first.getCard(), second.getCard());
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Scourge of Nel Toth");
    }

    @Test
    void cannotSacrificeAnOpponentsCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ScourgeOfNelToth());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ScourgeOfNelToth());
        harness.setGraveyard(player1, List.of(new ScourgeOfNelToth()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castFromGraveyardWithSacrifices(
                player1, 0, null, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Scourge of Nel Toth");
        harness.assertOnBattlefield(player2, "Scourge of Nel Toth");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Scourge of Nel Toth");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void insufficientBlackManaDoesNotSacrificeCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ScourgeOfNelToth());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ScourgeOfNelToth());
        harness.setGraveyard(player1, List.of(new ScourgeOfNelToth()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromGraveyardWithSacrifices(
                player1, 0, null, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Scourge of Nel Toth");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void graveyardPermissionDoesNotAllowCastingOutsideMainPhase() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ScourgeOfNelToth());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ScourgeOfNelToth());
        harness.setGraveyard(player1, List.of(new ScourgeOfNelToth()));
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castFromGraveyardWithSacrifices(
                player1, 0, null, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Scourge of Nel Toth");
    }

    @Test
    void castsFromHandAtNormalCostWithoutSacrificingCreatures() {
        harness.castFromHand(player1, new ScourgeOfNelToth(), "{5}{B}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Scourge of Nel Toth");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void cannotSacrificeANoncreaturePermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ScourgeOfNelToth());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.setGraveyard(player1, List.of(new ScourgeOfNelToth()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castFromGraveyardWithSacrifices(
                player1, 0, null, List.of(creature.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(creature, land);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Scourge of Nel Toth");
        assertThat(gd.stack).isEmpty();
    }
}
