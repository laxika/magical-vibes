package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Wurmquake.class})
class WurmquakeTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one mana-sized Phyrexian Wurm without corrupted opponents")
    void createsOneWurmWithoutCorruptedOpponents() {
        harness.setHand(player1, List.of(new Wurmquake()));
        gd.playerPoisonCounters.put(player2.getId(), 2);
        addRegularMana();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(findWurms()).hasSize(1);
        assertWurm(findWurms().get(0), 6);
    }

    @Test
    @DisplayName("Creates another Wurm for each opponent with at least three poison counters")
    void createsAdditionalWurmsForCorruptedOpponents() {
        harness.setHand(player1, List.of(new Wurmquake()));
        gd.playerPoisonCounters.put(player2.getId(), 3);
        addRegularMana();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(findWurms()).hasSize(2);
        findWurms().forEach(wurm -> assertWurm(wurm, 6));
    }

    @Test
    @DisplayName("Flashback sizes the Wurm by its flashback cost and exiles Wurmquake")
    void flashbackCreatesTenTenWurmAndExilesSelf() {
        Wurmquake wurmquake = new Wurmquake();
        harness.setGraveyard(player1, List.of(wurmquake));
        addFlashbackMana();

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(findWurms()).hasSize(1);
        assertWurm(findWurms().get(0), 10);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(wurmquake.getId()));
    }

    private void addRegularMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void addFlashbackMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);
    }

    private List<Permanent> findWurms() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> "Phyrexian Wurm".equals(permanent.getCard().getName()))
                .toList();
    }

    private void assertWurm(Permanent wurm, int size) {
        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(size);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(size);
        assertThat(gqs.hasKeyword(gd, wurm, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, wurm, Keyword.TOXIC)).isTrue();
    }
}
