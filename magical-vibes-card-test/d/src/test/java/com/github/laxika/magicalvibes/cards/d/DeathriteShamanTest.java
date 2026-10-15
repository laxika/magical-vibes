package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AnnihilatingFire;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeathriteShaman.class, Forest.class, DrainpipeVermin.class, AnnihilatingFire.class, MindRot.class})
class DeathriteShamanTest extends BaseCardTest {

    @Test
    void exilesLandAndAddsChosenMana() {
        Permanent shaman = addReadyShaman(player1);
        Card land = new Forest();
        harness.setGraveyard(player2, List.of(land));

        harness.activateAbility(player1, battlefieldIndex(player1, shaman), 0, null, land.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(land);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void exilesInstantOrSorceryAndEachOpponentLosesLife() {
        Permanent shaman = addReadyShaman(player1);
        Card instant = new AnnihilatingFire();
        harness.setGraveyard(player2, List.of(instant));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, shaman), 1, null, instant.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(instant);
        harness.assertLife(player2, 18);
    }

    @Test
    void exilesCreatureAndGainsLife() {
        Permanent shaman = addReadyShaman(player1);
        Card creature = new DrainpipeVermin();
        harness.setGraveyard(player2, List.of(creature));
        harness.setLife(player1, 10);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, battlefieldIndex(player1, shaman), 2, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature);
        harness.assertLife(player1, 12);
    }

    @Test
    void rejectsTargetWithWrongCardType() {
        Permanent shaman = addReadyShaman(player1);
        Card instant = new AnnihilatingFire();
        harness.setGraveyard(player2, List.of(instant));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, shaman), 0, null, instant.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fizzlesWithoutLifeGainWhenCreatureLeavesGraveyard() {
        Permanent shaman = addReadyShaman(player1);
        Card creature = new DrainpipeVermin();
        harness.setGraveyard(player2, List.of(creature));
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, shaman), 2, null, creature.getId(), Zone.GRAVEYARD);
        gd.playerGraveyards.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(creature);
    }

    @Test
    void exilesSorceryFromOwnGraveyardWithoutLosingControllerLife() {
        Permanent shaman = addReadyShaman(player1);
        Card sorcery = new MindRot();
        harness.setGraveyard(player1, List.of(sorcery));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, shaman), 1, null, sorcery.getId(), Zone.GRAVEYARD);
        assertThat(shaman.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(sorcery);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    void landAbilityUsesStackAndAddsNoManaWhenTargetLeavesGraveyard() {
        Permanent shaman = addReadyShaman(player1);
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land));

        harness.activateAbility(player1, battlefieldIndex(player1, shaman), 0, null, land.getId(), Zone.GRAVEYARD);

        assertThat(gd.stack).hasSize(1);
        assertThat(shaman.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        gd.playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(land);
    }

    @Test
    void fizzlesWithoutLifeLossWhenInstantLeavesGraveyard() {
        Permanent shaman = addReadyShaman(player1);
        Card instant = new AnnihilatingFire();
        harness.setGraveyard(player2, List.of(instant));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, shaman), 1, null, instant.getId(), Zone.GRAVEYARD);
        gd.playerGraveyards.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(instant);
        assertThat(shaman.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void cannotActivateWithoutTarget(int abilityIndex) {
        Permanent shaman = addReadyShaman(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, shaman), abilityIndex, null, null, Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void summoningSicknessPreventsAllTapAbilities(int abilityIndex) {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new DeathriteShaman());
        shaman.setSummoningSick(true);
        List<Card> targets = List.of(new Forest(), new AnnihilatingFire(), new DrainpipeVermin());
        harness.setGraveyard(player2, targets);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, shaman), abilityIndex, null,
                targets.get(abilityIndex).getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void canChooseAnyManaColorAfterSourceLeavesBattlefield(ManaColor color) {
        Permanent shaman = addReadyShaman(player1);
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land));

        harness.activateAbility(player1, battlefieldIndex(player1, shaman), 0, null, land.getId(), Zone.GRAVEYARD);
        gd.playerBattlefields.get(player1.getId()).remove(shaman);
        harness.passBothPriorities();
        harness.handleListChoice(player1, color.name());

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(land);
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void rejectsLandForLifeAbilities(int abilityIndex) {
        Permanent shaman = addReadyShaman(player1);
        Card land = new Forest();
        harness.setGraveyard(player2, List.of(land));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, shaman), abilityIndex, null, land.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void lifeAbilitiesRequireColoredMana(int abilityIndex) {
        Permanent shaman = addReadyShaman(player1);
        Card target = abilityIndex == 1 ? new MindRot() : new DrainpipeVermin();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, shaman), abilityIndex, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyShaman(Player player) {
        return addCreatureReady(player, new DeathriteShaman());
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
