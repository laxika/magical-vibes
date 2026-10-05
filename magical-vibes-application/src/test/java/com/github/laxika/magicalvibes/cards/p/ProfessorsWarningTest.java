package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.m.MortalitySpear;
import com.github.laxika.magicalvibes.cards.s.StoneriseSpirit;
import com.github.laxika.magicalvibes.cards.w.WitherbloomCampus;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ProfessorsWarning.class, StoneriseSpirit.class, WitherbloomCampus.class, MortalitySpear.class})
class ProfessorsWarningTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on the target creature")
    void putsCounterOnTargetCreature() {
        harness.addToBattlefield(player1, new StoneriseSpirit());
        harness.setHand(player1, List.of(new ProfessorsWarning()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID targetId = harness.getPermanentId(player1, "Stonerise Spirit");
        harness.castInstant(player1, 0, 0, targetId);
        harness.passBothPriorities();

        Permanent spirit = findPermanent(player1, "Stonerise Spirit");
        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gives the target creature indestructible until end of turn")
    void grantsIndestructibleToTargetCreature() {
        harness.addToBattlefield(player1, new StoneriseSpirit());
        harness.setHand(player1, List.of(new ProfessorsWarning()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID targetId = harness.getPermanentId(player1, "Stonerise Spirit");
        harness.castInstant(player1, 0, 1, targetId);
        harness.passBothPriorities();

        Permanent spirit = findPermanent(player1, "Stonerise Spirit");
        assertThat(spirit.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new WitherbloomCampus());
        harness.setHand(player1, List.of(new ProfessorsWarning()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID targetId = harness.getPermanentId(player1, "Witherbloom Campus");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Both modes can target an opponent's creature and only apply the chosen effect")
    void canTargetOpponentsCreature(int mode) {
        harness.addToBattlefield(player2, new StoneriseSpirit());
        Permanent spirit = findPermanent(player2, "Stonerise Spirit");
        harness.setHand(player1, List.of(new ProfessorsWarning()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, mode, spirit.getId());
        harness.passBothPriorities();

        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(mode == 0 ? 1 : 0);
        assertThat(spirit.hasKeyword(Keyword.INDESTRUCTIBLE)).isEqualTo(mode == 1);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Counters persist through cleanup but indestructible expires")
    void durationMatchesChosenMode(int mode) {
        harness.addToBattlefield(player1, new StoneriseSpirit());
        Permanent spirit = findPermanent(player1, "Stonerise Spirit");
        harness.setHand(player1, List.of(new ProfessorsWarning()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, mode, spirit.getId());
        harness.passBothPriorities();
        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(mode == 0 ? 1 : 0);
        assertThat(spirit.hasKeyword(Keyword.INDESTRUCTIBLE)).isEqualTo(mode == 1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(mode == 0 ? 1 : 0);
        assertThat(spirit.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Indestructible mode cannot target a noncreature permanent")
    void indestructibleModeCannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new WitherbloomCampus());
        harness.setHand(player1, List.of(new ProfessorsWarning()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID targetId = harness.getPermanentId(player1, "Witherbloom Campus");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Neither mode affects a creature that leaves before resolution")
    void targetLeavingBeforeResolutionMakesSpellFail(int mode) {
        harness.addToBattlefield(player1, new StoneriseSpirit());
        Permanent spirit = findPermanent(player1, "Stonerise Spirit");
        harness.setHand(player1, List.of(new ProfessorsWarning()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, mode, spirit.getId());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, spirit);
        harness.passBothPriorities();

        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(spirit.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        harness.assertNotOnBattlefield(player1, "Stonerise Spirit");
        harness.assertInGraveyard(player1, "Professor's Warning");
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Only the indestructible mode prevents destruction")
    void onlyIndestructibleModePreventsDestruction(int mode) {
        harness.addToBattlefield(player1, new StoneriseSpirit());
        UUID targetId = harness.getPermanentId(player1, "Stonerise Spirit");
        harness.setHand(player1, List.of(new ProfessorsWarning()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, mode, targetId);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new MortalitySpear()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0, targetId);

        if (mode == 1) {
            harness.assertOnBattlefield(player1, "Stonerise Spirit");
            harness.assertNotInGraveyard(player1, "Stonerise Spirit");
        } else {
            harness.assertNotOnBattlefield(player1, "Stonerise Spirit");
            harness.assertInGraveyard(player1, "Stonerise Spirit");
        }
    }
}
