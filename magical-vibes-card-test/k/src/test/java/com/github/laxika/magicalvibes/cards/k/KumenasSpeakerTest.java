package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AquitectsWill;
import com.github.laxika.magicalvibes.cards.a.AshayaSoulOfTheWild;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.j.JadeGuardian;
import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KumenasSpeaker.class, Island.class, JadeGuardian.class, RaptorCompanion.class,
        AshayaSoulOfTheWild.class, AquitectsWill.class})
class KumenasSpeakerTest extends BaseCardTest {

    @Test
    @DisplayName("Base 1/1 when no other Merfolk or Island is controlled")
    void noBoostWhenAlone() {
        Permanent speaker = harness.addToBattlefieldAndReturn(player1, new KumenasSpeaker());

        assertThat(gqs.getEffectivePower(gd, speaker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, speaker)).isEqualTo(1);
    }

    @Test
    @DisplayName("No boost with a non-Merfolk, non-Island creature")
    void noBoostWithIrrelevantCreature() {
        Permanent speaker = harness.addToBattlefieldAndReturn(player1, new KumenasSpeaker());
        harness.addToBattlefield(player1, new RaptorCompanion());

        assertThat(gqs.getEffectivePower(gd, speaker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, speaker)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +1/+1 when controller controls another Merfolk")
    void boostWithAnotherMerfolk() {
        Permanent speaker = harness.addToBattlefieldAndReturn(player1, new KumenasSpeaker());
        harness.addToBattlefield(player1, new JadeGuardian());

        assertThat(gqs.getEffectivePower(gd, speaker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, speaker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets +1/+1 when controller controls an Island")
    void boostWithIsland() {
        Permanent speaker = harness.addToBattlefieldAndReturn(player1, new KumenasSpeaker());
        harness.addToBattlefield(player1, new Island());

        assertThat(gqs.getEffectivePower(gd, speaker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, speaker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost is +1/+1 even when controlling both another Merfolk and an Island")
    void boostDoesNotStackWithMerfolkAndIsland() {
        Permanent speaker = harness.addToBattlefieldAndReturn(player1, new KumenasSpeaker());
        harness.addToBattlefield(player1, new JadeGuardian());
        harness.addToBattlefield(player1, new Island());

        assertThat(gqs.getEffectivePower(gd, speaker)).isEqualTo(2); // 1 base + 1 boost, not +2
        assertThat(gqs.getEffectiveToughness(gd, speaker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two Kumena's Speakers boost each other")
    void twoSpeakersBoostEachOther() {
        harness.addToBattlefield(player1, new KumenasSpeaker());
        harness.addToBattlefield(player1, new KumenasSpeaker());

        List<Permanent> speakers = findPermanents(player1, "Kumena's Speaker");

        assertThat(speakers).hasSize(2);
        // Each sees the other as "another Merfolk" so both get the boost
        for (Permanent speaker : speakers) {
            assertThat(gqs.getEffectivePower(gd, speaker)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, speaker)).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("Opponent's Merfolk does not grant the boost")
    void opponentMerfolkDoesNotCount() {
        Permanent speaker = harness.addToBattlefieldAndReturn(player1, new KumenasSpeaker());
        harness.addToBattlefield(player2, new JadeGuardian());

        assertThat(gqs.getEffectivePower(gd, speaker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, speaker)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's Island does not grant the boost")
    void opponentIslandDoesNotCount() {
        Permanent speaker = harness.addToBattlefieldAndReturn(player1, new KumenasSpeaker());
        harness.addToBattlefield(player2, new Island());

        assertThat(gqs.getEffectivePower(gd, speaker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, speaker)).isEqualTo(1);
    }

    @Test
    @DisplayName("Loses boost when the other Merfolk leaves the battlefield")
    void losesBoostWhenMerfolkLeaves() {
        Permanent speaker = harness.addToBattlefieldAndReturn(player1, new KumenasSpeaker());
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new JadeGuardian());

        assertThat(gqs.getEffectivePower(gd, speaker)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(merfolk);

        assertThat(gqs.getEffectivePower(gd, speaker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, speaker)).isEqualTo(1);
    }

    @Test
    @DisplayName("Static boost survives end-of-turn modifier reset")
    void staticBoostSurvivesEndOfTurnReset() {
        Permanent speaker = harness.addToBattlefieldAndReturn(player1, new KumenasSpeaker());
        harness.addToBattlefield(player1, new Island());

        assertThat(gqs.getEffectivePower(gd, speaker)).isEqualTo(2);

        speaker.resetModifiers();

        assertThat(gqs.getEffectivePower(gd, speaker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, speaker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Speaker itself satisfies the Island condition when it retains its ability")
    @CardUsed({KumenasSpeaker.class, AshayaSoulOfTheWild.class, AquitectsWill.class, Island.class})
    void speakerItselfCanBeTheIsland() {
        Permanent speaker = harness.addToBattlefieldAndReturn(player1, new KumenasSpeaker());
        harness.addToBattlefield(player1, new AshayaSoulOfTheWild());
        harness.setHand(player1, List.of(new AquitectsWill()));
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, speaker)).isEqualTo(1);
        harness.castAndResolveSorcery(player1, 0, 0, speaker.getId());

        assertThat(gqs.effectiveBasicLandTypes(gd, speaker)).contains(CardSubtype.ISLAND);
        assertThat(gqs.getEffectivePower(gd, speaker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, speaker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Loses boost when the only Island leaves the battlefield")
    void losesBoostWhenIslandLeaves() {
        Permanent speaker = harness.addToBattlefieldAndReturn(player1, new KumenasSpeaker());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        assertThat(gqs.getEffectivePower(gd, speaker)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(island);

        assertThat(gqs.getEffectivePower(gd, speaker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, speaker)).isEqualTo(1);
    }

}
