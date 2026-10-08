package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.h.HiddenRetreat;
import com.github.laxika.magicalvibes.cards.h.HonorGuard;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VolrathsStronghold.class, HonorGuard.class, HiddenRetreat.class})
class VolrathsStrongholdTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one colorless mana")
    void tapsForColorless() {
        Permanent stronghold = addStronghold();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(stronghold.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Puts a target creature card from the graveyard on top of the library")
    void putsTargetCreatureOnTopOfLibrary() {
        int strongholdIndex = addStrongholdIndex();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        Card creature = new HonorGuard();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(new HiddenRetreat()));

        harness.activateAbilityWithGraveyardTargets(player1, strongholdIndex, 1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(findPermanent(player1, "Volrath's Stronghold").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only creature cards in your graveyard are legal targets")
    void rejectsInvalidGraveyardTargets() {
        int strongholdIndex = addStrongholdIndex();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        Card nonCreature = new HiddenRetreat();
        Card opponentCreature = new HonorGuard();
        harness.setGraveyard(player1, List.of(nonCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, strongholdIndex, 1, List.of(nonCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, strongholdIndex, 1, List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires a creature card target in your graveyard")
    void requiresCreatureTarget() {
        int strongholdIndex = addStrongholdIndex();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, strongholdIndex, 1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(findPermanent(player1, "Volrath's Stronghold").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not return another creature when the target leaves the graveyard")
    void doesNothingWhenTargetLeavesGraveyard() {
        int strongholdIndex = addStrongholdIndex();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        Card target = new HonorGuard();
        Card otherCreature = new HonorGuard();
        Card libraryCard = new HiddenRetreat();
        harness.setGraveyard(player1, List.of(target, otherCreature));
        harness.setLibrary(player1, List.of(libraryCard));

        harness.activateAbilityWithGraveyardTargets(player1, strongholdIndex, 1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(otherCreature));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherCreature);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(findPermanent(player1, "Volrath's Stronghold").isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can put a creature onto an empty library")
    void returnsCreatureToEmptyLibrary() {
        int strongholdIndex = addStrongholdIndex();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        Card target = new HonorGuard();
        Card otherCreature = new HonorGuard();
        harness.setGraveyard(player1, List.of(target, otherCreature));
        harness.setLibrary(player1, List.of());

        harness.activateAbilityWithGraveyardTargets(player1, strongholdIndex, 1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherCreature);
    }

    @Test
    @DisplayName("Cannot pay the black mana cost with only colorless mana")
    void requiresBlackMana() {
        Permanent stronghold = addStronghold();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Card target = new HonorGuard();
        harness.setGraveyard(player1, List.of(target));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 1, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(stronghold.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate the graveyard ability after tapping for mana")
    void cannotActivateWhileTapped() {
        Permanent stronghold = addStronghold();
        harness.addMana(player1, ManaColor.BLACK, 1);
        Card target = new HonorGuard();
        harness.setGraveyard(player1, List.of(target));
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 1, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(stronghold.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addStronghold() {
        return harness.addToBattlefieldAndReturn(player1, new VolrathsStronghold());
    }

    private int addStrongholdIndex() {
        Permanent stronghold = addStronghold();
        return gd.playerBattlefields.get(player1.getId()).indexOf(stronghold);
    }
}
