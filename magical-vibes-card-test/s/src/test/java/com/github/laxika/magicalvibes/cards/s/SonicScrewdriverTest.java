package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SonicScrewdriver.class, AngelsFeather.class, Forest.class, GrizzlyBears.class})
class SonicScrewdriverTest extends BaseCardTest {

    @Test
    @DisplayName("Adds one mana of a chosen color")
    void addsAnyColorMana() {
        addReadySonicScrewdriver(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Untaps another target artifact")
    void untapsAnotherArtifact() {
        addReadySonicScrewdriver(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AngelsFeather());
        artifact.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The untap ability cannot target Sonic Screwdriver itself")
    void cannotUntapItself() {
        Permanent screwdriver = addReadySonicScrewdriver(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, screwdriver.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Scries 1 after paying {2}")
    void scriesOne() {
        addReadySonicScrewdriver(player1);
        Card top = new Forest();
        Card next = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top, next));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(next);
    }

    @Test
    @DisplayName("Makes a target creature unblockable until end of turn")
    void makesCreatureUnblockable() {
        addReadySonicScrewdriver(player1);
        Permanent creature = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 3, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("The unblockable ability cannot target a noncreature")
    void cannotMakeNoncreatureUnblockable() {
        addReadySonicScrewdriver(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void newlyEnteredArtifactCanProduceEachColor(ManaColor color) {
        Permanent screwdriver = harness.addToBattlefieldAndReturn(player1, new SonicScrewdriver());
        screwdriver.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(screwdriver.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void tappedScrewdriverCannotActivateAnyAbility(int abilityIndex) {
        Permanent screwdriver = addReadySonicScrewdriver(player1);
        screwdriver.tap();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SonicScrewdriver());
        Permanent creature = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null,
                abilityIndex == 1 ? artifact.getId() : abilityIndex == 3 ? creature.getId() : null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void untapsAnotherScrewdriverYouControl() {
        Permanent source = addReadySonicScrewdriver(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SonicScrewdriver());
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());

        assertThat(source.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    void cannotUntapNonartifactCreature() {
        addReadySonicScrewdriver(player1);
        Permanent creature = addReadyCreature(player2);
        creature.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void untapAbilityStillResolvesAfterSourceLeavesBattlefield() {
        Permanent source = addReadySonicScrewdriver(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SonicScrewdriver());
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void unblockableAbilityDoesNothingWhenTargetLeavesBattlefield() {
        Permanent source = addReadySonicScrewdriver(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 3, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
        assertThat(source.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void scryCanKeepTopCard() {
        Permanent screwdriver = addReadySonicScrewdriver(player1);
        Card top = new Forest();
        Card next = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top, next));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, next);
        assertThat(screwdriver.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void scryEmptyLibraryDoesNotRequireAChoice() {
        Permanent screwdriver = addReadySonicScrewdriver(player1);
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(screwdriver.isTapped()).isTrue();
    }

    @Test
    void unblockableAbilityCanTargetYourCreature() {
        Permanent screwdriver = addReadySonicScrewdriver(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 3, null, creature.getId());

        assertThat(creature.isCantBeBlocked()).isFalse();
        assertThat(screwdriver.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();

        assertThat(creature.isCantBeBlocked()).isTrue();
    }

    private Permanent addReadySonicScrewdriver(Player player) {
        Permanent screwdriver = harness.addToBattlefieldAndReturn(player, new SonicScrewdriver());
        screwdriver.setSummoningSick(false);
        return screwdriver;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }
}
