package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkirsdagHighPriestTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot activate ability without morbid (no creature died this turn)")
    void cannotActivateWithoutMorbid() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new SkirsdagHighPriest());
        Permanent priest = findPermanent(player1, "Skirsdag High Priest");
        priest.setSummoningSick(false);

        addReadyCreature(player1);
        addReadyCreature(player1);

        int priestIdx = gd.playerBattlefields.get(player1.getId()).indexOf(priest);

        assertThatThrownBy(() -> harness.activateAbility(player1, priestIdx, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Morbid");
    }

    @Test
    @DisplayName("Creates a 5/5 black Demon token with flying when morbid is met")
    void createsDemonTokenWithMorbid() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new SkirsdagHighPriest());
        Permanent priest = findPermanent(player1, "Skirsdag High Priest");
        priest.setSummoningSick(false);

        // Exactly 2 other creatures — engine auto-pays the tap cost
        addReadyCreature(player1);
        addReadyCreature(player1);

        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        int priestIdx = gd.playerBattlefields.get(player1.getId()).indexOf(priest);
        harness.activateAbility(player1, priestIdx, null, null);
        harness.passBothPriorities(); // resolve ability

        // Verify a 5/5 Demon token with flying was created
        Permanent demon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Demon"))
                .findFirst().orElseThrow();
        assertThat(demon.getCard().getPower()).isEqualTo(5);
        assertThat(demon.getCard().getToughness()).isEqualTo(5);
        assertThat(demon.getCard().getSubtypes()).contains(CardSubtype.DEMON);
        assertThat(demon.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Priest and tapped creatures are tapped after activation")
    void priestAndCreaturesTappedAfterActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new SkirsdagHighPriest());
        Permanent priest = findPermanent(player1, "Skirsdag High Priest");
        priest.setSummoningSick(false);

        Permanent creature1 = addReadyCreature(player1);
        Permanent creature2 = addReadyCreature(player1);

        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        int priestIdx = gd.playerBattlefields.get(player1.getId()).indexOf(priest);
        harness.activateAbility(player1, priestIdx, null, null);

        // All three should be tapped (priest from {T}, creatures auto-tapped as cost)
        assertThat(priest.isTapped()).isTrue();
        assertThat(creature1.isTapped()).isTrue();
        assertThat(creature2.isTapped()).isTrue();

        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Cannot activate without enough creatures to tap")
    void cannotActivateWithoutEnoughCreatures() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new SkirsdagHighPriest());
        Permanent priest = findPermanent(player1, "Skirsdag High Priest");
        priest.setSummoningSick(false);

        // Only one other creature (need two)
        addReadyCreature(player1);

        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        int priestIdx = gd.playerBattlefields.get(player1.getId()).indexOf(priest);

        assertThatThrownBy(() -> harness.activateAbility(player1, priestIdx, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new SkirsdagHighPriest());
        // Priest has summoning sickness (default)

        addReadyCreature(player1);
        addReadyCreature(player1);

        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Interactive creature choice when more than 2 other creatures available")
    void interactiveCreatureChoice() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new SkirsdagHighPriest());
        Permanent priest = findPermanent(player1, "Skirsdag High Priest");
        priest.setSummoningSick(false);

        // 3 other creatures — more than 2 needed, so interactive choice
        Permanent creature1 = addReadyCreature(player1);
        Permanent creature2 = addReadyCreature(player1);
        Permanent creature3 = addReadyCreature(player1);

        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        int priestIdx = gd.playerBattlefields.get(player1.getId()).indexOf(priest);
        harness.activateAbility(player1, priestIdx, null, null);

        // Choose 2 of the 3 creatures to tap
        harness.handlePermanentChosen(player1, creature1.getId());
        harness.handlePermanentChosen(player1, creature2.getId());

        harness.passBothPriorities();

        // Demon token created
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Demon"));

        // Chosen creatures tapped, unchosen creature untapped
        assertThat(creature1.isTapped()).isTrue();
        assertThat(creature2.isTapped()).isTrue();
        assertThat(creature3.isTapped()).isFalse();
    }

    // ===== Helpers =====

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

}
