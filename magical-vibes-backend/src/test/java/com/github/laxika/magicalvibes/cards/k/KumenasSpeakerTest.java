package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.j.JadeGuardian;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KumenasSpeakerTest extends BaseCardTest {

    // ===== Base stats without condition met =====

    @Test
    @DisplayName("Base 1/1 when no other Merfolk or Island is controlled")
    void noBoostWhenAlone() {
        harness.addToBattlefield(player1, new KumenasSpeaker());

        Permanent speaker = findPermanent(player1, "Kumena's Speaker");
        assertThat(gqs.getEffectivePower(gd, speaker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, speaker)).isEqualTo(1);
    }

    @Test
    @DisplayName("No boost with a non-Merfolk, non-Island creature")
    void noBoostWithIrrelevantCreature() {
        harness.addToBattlefield(player1, new KumenasSpeaker());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent speaker = findPermanent(player1, "Kumena's Speaker");
        assertThat(gqs.getEffectivePower(gd, speaker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, speaker)).isEqualTo(1);
    }

    // ===== Boost with another Merfolk =====

    @Test
    @DisplayName("Gets +1/+1 when controller controls another Merfolk")
    void boostWithAnotherMerfolk() {
        harness.addToBattlefield(player1, new KumenasSpeaker());
        harness.addToBattlefield(player1, new JadeGuardian());

        Permanent speaker = findPermanent(player1, "Kumena's Speaker");
        assertThat(gqs.getEffectivePower(gd, speaker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, speaker)).isEqualTo(2);
    }

    // ===== Boost with an Island =====

    @Test
    @DisplayName("Gets +1/+1 when controller controls an Island")
    void boostWithIsland() {
        harness.addToBattlefield(player1, new KumenasSpeaker());
        harness.addToBattlefield(player1, new Island());

        Permanent speaker = findPermanent(player1, "Kumena's Speaker");
        assertThat(gqs.getEffectivePower(gd, speaker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, speaker)).isEqualTo(2);
    }

    // ===== Boost does not stack =====

    @Test
    @DisplayName("Boost is +1/+1 even when controlling both another Merfolk and an Island")
    void boostDoesNotStackWithMerfolkAndIsland() {
        harness.addToBattlefield(player1, new KumenasSpeaker());
        harness.addToBattlefield(player1, new JadeGuardian());
        harness.addToBattlefield(player1, new Island());

        Permanent speaker = findPermanent(player1, "Kumena's Speaker");
        assertThat(gqs.getEffectivePower(gd, speaker)).isEqualTo(2); // 1 base + 1 boost, not +2
        assertThat(gqs.getEffectiveToughness(gd, speaker)).isEqualTo(2);
    }

    // ===== "Another" — self doesn't count =====

    @Test
    @DisplayName("Two Kumena's Speakers alone do not boost each other (each has one other Merfolk)")
    void twoSpeakersBoostEachOther() {
        harness.addToBattlefield(player1, new KumenasSpeaker());
        harness.addToBattlefield(player1, new KumenasSpeaker());

        List<Permanent> speakers = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Kumena's Speaker"))
                .toList();

        assertThat(speakers).hasSize(2);
        // Each sees the other as "another Merfolk" so both get the boost
        for (Permanent speaker : speakers) {
            assertThat(gqs.getEffectivePower(gd, speaker)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, speaker)).isEqualTo(2);
        }
    }

    // ===== Opponent's permanents don't count =====

    @Test
    @DisplayName("Opponent's Merfolk does not grant the boost")
    void opponentMerfolkDoesNotCount() {
        harness.addToBattlefield(player1, new KumenasSpeaker());
        harness.addToBattlefield(player2, new JadeGuardian());

        Permanent speaker = findPermanent(player1, "Kumena's Speaker");
        assertThat(gqs.getEffectivePower(gd, speaker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, speaker)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's Island does not grant the boost")
    void opponentIslandDoesNotCount() {
        harness.addToBattlefield(player1, new KumenasSpeaker());
        harness.addToBattlefield(player2, new Island());

        Permanent speaker = findPermanent(player1, "Kumena's Speaker");
        assertThat(gqs.getEffectivePower(gd, speaker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, speaker)).isEqualTo(1);
    }

    // ===== Loses boost when condition no longer met =====

    @Test
    @DisplayName("Loses boost when the other Merfolk leaves the battlefield")
    void losesBoostWhenMerfolkLeaves() {
        harness.addToBattlefield(player1, new KumenasSpeaker());
        harness.addToBattlefield(player1, createMerfolk());

        Permanent speaker = findPermanent(player1, "Kumena's Speaker");
        assertThat(gqs.getEffectivePower(gd, speaker)).isEqualTo(2);

        // Remove the Merfolk
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> !p.getCard().getName().equals("Kumena's Speaker")
                        && p.getCard().getSubtypes().contains(CardSubtype.MERFOLK));

        assertThat(gqs.getEffectivePower(gd, speaker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, speaker)).isEqualTo(1);
    }

    // ===== Static boost survives end-of-turn reset =====

    @Test
    @DisplayName("Static boost survives end-of-turn modifier reset")
    void staticBoostSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new KumenasSpeaker());
        harness.addToBattlefield(player1, new Island());

        Permanent speaker = findPermanent(player1, "Kumena's Speaker");
        assertThat(gqs.getEffectivePower(gd, speaker)).isEqualTo(2);

        speaker.resetModifiers();

        assertThat(gqs.getEffectivePower(gd, speaker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, speaker)).isEqualTo(2);
    }

    // ===== Helper methods =====

    private Card createMerfolk() {
        Card card = new GrizzlyBears();
        card.setSubtypes(List.of(CardSubtype.MERFOLK));
        return card;
    }

    private Permanent findPermanent(Player player, String cardName) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(cardName))
                .findFirst().orElseThrow();
    }
}
