package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BramblewoodParagon;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MothdustChangeling.class, ElvishWarrior.class, Mutavault.class, BramblewoodParagon.class})
class MothdustChangelingTest extends BaseCardTest {

    @Test
    @DisplayName("A summoning-sick Changeling can tap itself to gain flying")
    void summoningSickChangelingCanTapItself() {
        harness.castFromHand(player1, new MothdustChangeling(), "{U}");
        harness.passBothPriorities();
        Permanent changeling = findPermanent(player1, "Mothdust Changeling");
        assertThat(changeling.isSummoningSick()).isTrue();

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(changeling);
        harness.activateAbility(player1, idx, null, null);

        assertThat(changeling.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, changeling, Keyword.FLYING)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, changeling, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A tapped Changeling can tap another summoning-sick creature")
    void tappedChangelingCanTapSummoningSickCreature() {
        Permanent changeling = addCreatureReady(player1, new MothdustChangeling());
        changeling.tap();
        harness.castFromHand(player1, new ElvishWarrior(), "{G}{G}");
        harness.passBothPriorities();
        Permanent warrior = findPermanent(player1, "Elvish Warrior");
        assertThat(warrior.isSummoningSick()).isTrue();

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(changeling);
        harness.activateAbility(player1, idx, null, null);
        assertThat(warrior.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, changeling, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Changeling enters as a Warrior for Bramblewood Paragon")
    void changelingReceivesWarriorEntryCounter() {
        addCreatureReady(player1, new BramblewoodParagon());
        harness.castFromHand(player1, new MothdustChangeling(), "{U}");
        harness.passBothPriorities();

        Permanent changeling = findPermanent(player1, "Mothdust Changeling");
        assertThat(changeling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, changeling, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Taps another creature to gain flying until end of turn")
    void tapsAnotherCreatureToGainFlying() {
        Permanent changeling = addCreatureReady(player1, new MothdustChangeling());
        Permanent warrior = addCreatureReady(player1, new ElvishWarrior());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(changeling);
        harness.activateAbility(player1, idx, null, null);

        // Two untapped creatures -> choose which to tap
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, warrior.getId());
        harness.passBothPriorities();

        assertThat(warrior.isTapped()).isTrue();
        assertThat(changeling.isTapped()).isFalse();
        assertThat(changeling.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Can tap itself as the only untapped creature to gain flying")
    void tapsItselfToGainFlying() {
        Permanent changeling = addCreatureReady(player1, new MothdustChangeling());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(changeling);
        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities();

        assertThat(changeling.isTapped()).isTrue();
        assertThat(changeling.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent changeling = addCreatureReady(player1, new MothdustChangeling());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(changeling);
        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities();
        assertThat(changeling.getGrantedKeywords()).contains(Keyword.FLYING);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(changeling.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("Cannot activate with no untapped creature to tap")
    void cannotActivateWithoutUntappedCreature() {
        Permanent changeling = addCreatureReady(player1, new MothdustChangeling());
        changeling.tap();

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(changeling);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot tap an untapped noncreature permanent as the cost")
    void cannotTapNoncreaturePermanent() {
        Permanent changeling = addCreatureReady(player1, new MothdustChangeling());
        changeling.tap();
        harness.addToBattlefieldAndReturn(player1, new Mutavault());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(changeling);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
    }

    @Test
    @DisplayName("Cannot tap an opponent's creature as the cost")
    void cannotTapOpponentsCreature() {
        Permanent changeling = addCreatureReady(player1, new MothdustChangeling());
        changeling.tap();
        addCreatureReady(player2, new ElvishWarrior());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(changeling);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
    }
}
