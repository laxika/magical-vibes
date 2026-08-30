package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SoundTheCallTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Wolf that gets +1/+1 for each matching card in all graveyards")
    void createsWolfWithGraveyardScaling() {
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setGraveyard(player2, List.of(new SoundTheCall(), new Forest()));

        castSoundTheCall();

        Permanent wolf = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(3);
    }

    @Test
    @DisplayName("Wolf's bonus updates when a matching graveyard card leaves")
    void bonusUpdatesWithGraveyardChanges() {
        harness.setGraveyard(player2, List.of(new SoundTheCall()));

        castSoundTheCall();

        Permanent wolf = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(3);

        gd.playerGraveyards.get(player2.getId()).clear();

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
    }

    private void castSoundTheCall() {
        harness.setHand(player1, List.of(new SoundTheCall()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();
    }
}
