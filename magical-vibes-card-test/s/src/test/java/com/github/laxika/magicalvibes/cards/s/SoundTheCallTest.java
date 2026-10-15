package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoundTheCall.class, SnowCoveredForest.class})
class SoundTheCallTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Wolf that gets +1/+1 for each matching card in all graveyards")
    void createsWolfWithGraveyardScaling() {
        harness.setGraveyard(player1, List.of(new SnowCoveredForest()));
        harness.setGraveyard(player2, List.of(new SoundTheCall(), new SnowCoveredForest()));

        castSoundTheCall();

        Permanent wolf = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(3);
    }

    @Test
    @DisplayName("Counts matching cards in both players' graveyards")
    void countsMatchingCardsInBothGraveyards() {
        harness.setGraveyard(player1, List.of(new SoundTheCall()));
        harness.setGraveyard(player2, List.of(new SoundTheCall()));

        castSoundTheCall();

        Permanent wolf = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(4);
    }

    @Test
    @DisplayName("Wolf's bonus updates when a matching graveyard card leaves")
    void bonusUpdatesWithGraveyardChanges() {
        harness.setGraveyard(player2, List.of(new SoundTheCall()));

        castSoundTheCall();

        Permanent wolf = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(3);

        harness.setGraveyard(player2, List.of());

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
    }

    @Test
    @DisplayName("The resolved spell itself boosts its single Wolf, which shrinks when all matching cards leave")
    void countsResolvedSpellAndReturnsToBaseSizeWithEmptyGraveyards() {
        castSoundTheCall();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent wolf = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(wolf.getCard().isToken()).isTrue();
        harness.assertInGraveyard(player1, "Sound the Call");
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);

        harness.setGraveyard(player1, List.of());

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(1);
    }

    @Test
    @DisplayName("A later cast boosts both the earlier Wolf and the newly created Wolf")
    void successiveCastsUpdateEveryWolf() {
        castSoundTheCall();
        Permanent firstWolf = gd.playerBattlefields.get(player1.getId()).getFirst();

        castSoundTheCall();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2).contains(firstWolf);
        for (Permanent wolf : gd.playerBattlefields.get(player1.getId())) {
            assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(3);
        }
    }

    private void castSoundTheCall() {
        harness.castFromHand(player1, new SoundTheCall(), "{2}{G}");
        harness.passBothPriorities();
    }
}
