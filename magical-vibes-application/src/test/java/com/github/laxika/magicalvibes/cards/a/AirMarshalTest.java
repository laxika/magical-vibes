package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.y.YotianFrontliner;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AirMarshal.class, ArgothianSprite.class, YotianFrontliner.class})
class AirMarshalTest extends BaseCardTest {

    @Test
    @DisplayName("Ability grants flying to target Soldier")
    void abilityGrantsFlyingToSoldier() {
        addReadyAirMarshal(player1);
        Permanent soldier = addReadySoldier(player1);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, soldier.getId());
        harness.passBothPriorities();

        assertThat(soldier.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Granted flying is removed at end of turn")
    void flyingRemovedAtEndOfTurn() {
        addReadyAirMarshal(player1);
        Permanent soldier = addReadySoldier(player1);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, soldier.getId());
        harness.passBothPriorities();
        assertThat(soldier.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(soldier.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Ability cannot target a non-Soldier creature")
    void rejectsNonSoldierTarget() {
        addReadyAirMarshal(player1);
        Permanent nonSoldier = addReadyNonSoldier(player1);
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonSoldier.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetItselfWhileTappedAndSummoningSick() {
        Permanent marshal = harness.addToBattlefieldAndReturn(player1, new AirMarshal());
        marshal.setSummoningSick(true);
        marshal.setTapped(true);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, marshal.getId());
        harness.passBothPriorities();

        assertThat(marshal.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(marshal.isTapped()).isTrue();
    }

    @Test
    void canTargetOpponentsSoldier() {
        addReadyAirMarshal(player1);
        Permanent soldier = addReadySoldier(player2);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, soldier.getId());
        harness.passBothPriorities();

        assertThat(soldier.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    void cannotActivateWithOnlyTwoMana() {
        Permanent marshal = addReadyAirMarshal(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, marshal.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(marshal.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent marshal = addReadyAirMarshal(player1);
        Permanent soldier = addReadySoldier(player1);
        addAbilityMana();
        harness.activateAbility(player1, 0, null, soldier.getId());

        gd.playerBattlefields.get(player1.getId()).remove(marshal);
        harness.passBothPriorities();

        assertThat(soldier.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @CardUsed({AltarOfTheGoyf.class, ArtificialEvolution.class})
    void canTargetNoncreatureSoldierPermanent() {
        addReadyAirMarshal(player1);
        Permanent altar = harness.addToBattlefieldAndReturn(player1, new AltarOfTheGoyf());
        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, altar.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "LHURGOYF");
        harness.handleListChoice(player1, "SOLDIER");
        addAbilityMana();

        harness.activateAbility(player1, 0, null, altar.getId());
        harness.passBothPriorities();

        assertThat(altar.hasKeyword(Keyword.FLYING)).isTrue();
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private Permanent addReadyAirMarshal(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new AirMarshal());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addReadySoldier(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new YotianFrontliner());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addReadyNonSoldier(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new ArgothianSprite());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
