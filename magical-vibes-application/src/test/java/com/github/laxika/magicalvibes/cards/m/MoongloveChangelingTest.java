package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BramblewoodParagon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoongloveChangeling.class, BramblewoodParagon.class})
class MoongloveChangelingTest extends BaseCardTest {

    @Test
    @DisplayName("{B}: gains deathtouch until end of turn")
    void grantsDeathtouch() {
        Permanent changeling = addCreatureReady(player1, new MoongloveChangeling());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(changeling.getGrantedKeywords()).contains(Keyword.DEATHTOUCH);
    }

    @Test
    @DisplayName("Activation consumes exactly one black mana")
    void activationConsumesBlackMana() {
        addCreatureReady(player1, new MoongloveChangeling());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate without black mana")
    void cannotActivateWithoutBlackMana() {
        addCreatureReady(player1, new MoongloveChangeling());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Deathtouch wears off at end of turn")
    void deathtouchWearsOffAtEndOfTurn() {
        Permanent changeling = addCreatureReady(player1, new MoongloveChangeling());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(changeling.getGrantedKeywords()).contains(Keyword.DEATHTOUCH);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(changeling.getGrantedKeywords()).doesNotContain(Keyword.DEATHTOUCH);
    }

    @Test
    @DisplayName("A tapped, summoning-sick changeling can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent changeling = harness.addToBattlefieldAndReturn(player1, new MoongloveChangeling());
        changeling.setSummoningSick(true);
        changeling.setTapped(true);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, changeling, Keyword.DEATHTOUCH)).isTrue();
        assertThat(changeling.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Deathtouch is granted only on resolution and only to the source")
    void grantsOnlySourceDeathtouchOnResolution() {
        Permanent source = addCreatureReady(player1, new MoongloveChangeling());
        Permanent other = addCreatureReady(player1, new MoongloveChangeling());
        Permanent opponent = addCreatureReady(player2, new MoongloveChangeling());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gqs.hasKeyword(gd, source, Keyword.DEATHTOUCH)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, source, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Activated deathtouch destroys a blocker with more toughness than the damage dealt")
    void activatedDeathtouchDestroysLargerBlocker() {
        addCreatureReady(player1, new MoongloveChangeling());
        Permanent blocker = addCreatureReady(player2, new MoongloveChangeling());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Moonglove Changeling");
        harness.assertInGraveyard(player2, "Moonglove Changeling");
        harness.assertNotOnBattlefield(player2, "Moonglove Changeling");
    }

    @Test
    @DisplayName("Changeling qualifies for a Warrior entering-the-battlefield replacement effect")
    void changelingEntersWithWarriorCounter() {
        addCreatureReady(player1, new BramblewoodParagon());

        harness.castFromHand(player1, new MoongloveChangeling(), "{2}{B}");
        harness.passBothPriorities();

        Permanent changeling = findPermanent(player1, "Moonglove Changeling");
        assertThat(changeling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, changeling, Keyword.TRAMPLE)).isTrue();
    }
}
