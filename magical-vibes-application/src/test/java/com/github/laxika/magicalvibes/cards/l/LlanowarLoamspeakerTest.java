package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LlanowarLoamspeaker.class, Forest.class})
class LlanowarLoamspeakerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Llanowar Loamspeaker adds one mana of any color")
    void tapsForAnyColor() {
        addLoamspeakerReady(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sorcery-speed ability animates a land you control")
    void animatesTargetLand() {
        addLoamspeakerReady(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 1, null, land.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, land)).contains(CardSubtype.ELEMENTAL);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Land animation wears off at end of turn")
    void animationWearsOffAtEndOfTurn() {
        addLoamspeakerReady(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 1, null, land.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, land)).isFalse();
        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Animation ability can target only a land you control")
    void restrictsTargetsToControlledLands() {
        addLoamspeakerReady(player1);
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent creature = addCreatureReady(player1, new LlanowarLoamspeaker());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, opponentLand.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Animation cannot be activated outside a main phase")
    void cannotAnimateDuringCombat() {
        addLoamspeakerReady(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Animation cannot be activated on the opponent's turn")
    void cannotAnimateOnOpponentsTurn() {
        addLoamspeakerReady(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Animation cannot be activated while another ability is on the stack")
    void cannotAnimateWithNonemptyStack() {
        addLoamspeakerReady(player1);
        addLoamspeakerReady(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 1, null, land.getId());

        assertThat(gd.stack).hasSize(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Summoning sickness prevents both tap abilities")
    void summoningSicknessPreventsBothAbilities() {
        harness.addToBattlefield(player1, new LlanowarLoamspeaker());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Animated newly entered land retains its mana ability and can tap thanks to haste")
    void animatedLandRetainsManaAbilityAndCanTapImmediately() {
        Permanent loamspeaker = addLoamspeakerReady(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.setSummoningSick(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 1, null, land.getId());
        assertThat(loamspeaker.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED"})
    @DisplayName("Mana ability produces each other color immediately, even outside sorcery timing")
    void producesOtherColorsOutsideMainPhase(ManaColor color) {
        Permanent loamspeaker = addLoamspeakerReady(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(loamspeaker.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
    private Permanent addLoamspeakerReady(Player player) {
        return addCreatureReady(player, new LlanowarLoamspeaker());
    }
}
