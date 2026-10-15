package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AbbeyGriffin;
import com.github.laxika.magicalvibes.cards.v.VictimOfNight;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({SkirsdagHighPriest.class, AbbeyGriffin.class, VictimOfNight.class})
class SkirsdagHighPriestTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot activate ability without morbid (no creature died this turn)")
    void cannotActivateWithoutMorbid() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent priest = addCreatureReady(player1, new SkirsdagHighPriest());

        addCreatureReady(player1, new AbbeyGriffin());
        addCreatureReady(player1, new AbbeyGriffin());

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

        Permanent priest = addCreatureReady(player1, new SkirsdagHighPriest());

        // Exactly 2 other creatures — engine auto-pays the tap cost
        addCreatureReady(player1, new AbbeyGriffin());
        addCreatureReady(player1, new AbbeyGriffin());

        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        int priestIdx = gd.playerBattlefields.get(player1.getId()).indexOf(priest);
        harness.activateAbility(player1, priestIdx, null, null);
        harness.passBothPriorities(); // resolve ability

        // Verify a 5/5 Demon token with flying was created
        Permanent demon = findPermanent(player1, "Demon");
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

        Permanent priest = addCreatureReady(player1, new SkirsdagHighPriest());

        Permanent creature1 = addCreatureReady(player1, new AbbeyGriffin());
        Permanent creature2 = addCreatureReady(player1, new AbbeyGriffin());

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

        Permanent priest = addCreatureReady(player1, new SkirsdagHighPriest());

        // Only one other creature (need two)
        addCreatureReady(player1, new AbbeyGriffin());

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

        addCreatureReady(player1, new AbbeyGriffin());
        addCreatureReady(player1, new AbbeyGriffin());

        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Interactive creature choice when more than 2 other creatures available")
    void interactiveCreatureChoice() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent priest = addCreatureReady(player1, new SkirsdagHighPriest());

        // 3 other creatures — more than 2 needed, so interactive choice
        Permanent creature1 = addCreatureReady(player1, new AbbeyGriffin());
        Permanent creature2 = addCreatureReady(player1, new AbbeyGriffin());
        Permanent creature3 = addCreatureReady(player1, new AbbeyGriffin());

        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        int priestIdx = gd.playerBattlefields.get(player1.getId()).indexOf(priest);
        harness.activateAbility(player1, priestIdx, null, null);

        // Choose 2 of the 3 creatures to tap
        harness.handlePermanentChosen(player1, creature1.getId());
        harness.handlePermanentChosen(player1, creature2.getId());

        harness.passBothPriorities();

        // Demon token created
        harness.assertOnBattlefield(player1, "Demon");

        // Chosen creatures tapped, unchosen creature untapped
        assertThat(creature1.isTapped()).isTrue();
        assertThat(creature2.isTapped()).isTrue();
        assertThat(creature3.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Summoning-sick support creatures can pay the additional tap cost")
    void canTapSummoningSickSupportCreatures() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent priest = addCreatureReady(player1, new SkirsdagHighPriest());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AbbeyGriffin());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AbbeyGriffin());
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        harness.activateAbility(player1, 0, null, null);

        assertThat(priest.isTapped()).isTrue();
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(countPermanents(player1, "Demon")).isZero();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Demon")).isEqualTo(1);
        Permanent demon = findPermanent(player1, "Demon");
        assertThat(demon.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(demon.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapped and opposing creatures cannot pay the additional tap cost")
    void cannotUseTappedOrOpposingCreatures() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent priest = addCreatureReady(player1, new SkirsdagHighPriest());
        Permanent ready = addCreatureReady(player1, new AbbeyGriffin());
        Permanent tapped = addCreatureReady(player1, new AbbeyGriffin());
        tapped.tap();
        Permanent opposing = addCreatureReady(player2, new AbbeyGriffin());
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(priest.isTapped()).isFalse();
        assertThat(ready.isTapped()).isFalse();
        assertThat(opposing.isTapped()).isFalse();
        assertThat(countPermanents(player1, "Demon")).isZero();
    }

    @Test
    @DisplayName("A real opposing creature death enables activation during the opponent's turn")
    void opposingCreatureDeathEnablesActivationOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addCreatureReady(player1, new SkirsdagHighPriest());
        addCreatureReady(player1, new AbbeyGriffin());
        addCreatureReady(player1, new AbbeyGriffin());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new AbbeyGriffin());
        harness.setHand(player1, List.of(new VictimOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, victim.getId());
        harness.assertInGraveyard(player2, "Abbey Griffin");
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Demon")).isEqualTo(1);
        assertThat(countPermanents(player2, "Demon")).isZero();
    }

    @Test
    @DisplayName("Removing the Priest after activation does not stop token creation")
    void resolvesAfterPriestDies() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent priest = addCreatureReady(player1, new SkirsdagHighPriest());
        addCreatureReady(player1, new AbbeyGriffin());
        addCreatureReady(player1, new AbbeyGriffin());
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);
        harness.setHand(player2, List.of(new VictimOfNight()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.castAndResolveInstant(player2, 0, priest.getId());
        harness.assertInGraveyard(player1, "Skirsdag High Priest");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Demon")).isEqualTo(1);
    }

    @Test
    @DisplayName("An already tapped Priest cannot activate again")
    void cannotActivateTappedPriest() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent priest = addCreatureReady(player1, new SkirsdagHighPriest());
        priest.tap();
        addCreatureReady(player1, new AbbeyGriffin());
        addCreatureReady(player1, new AbbeyGriffin());
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Demon")).isZero();
    }
}
