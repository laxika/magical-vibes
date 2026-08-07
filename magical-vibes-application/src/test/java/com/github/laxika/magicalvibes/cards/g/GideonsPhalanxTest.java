package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GideonsPhalanxTest extends BaseCardTest {

    @Test
    @DisplayName("Creates four 2/2 Knight tokens with vigilance")
    void createsFourKnights() {
        cast(player1);

        List<Permanent> knights = knightsOf(player1);
        assertThat(knights).hasSize(4);
        for (Permanent knight : knights) {
            assertThat(knight.getEffectivePower()).isEqualTo(2);
            assertThat(knight.getEffectiveToughness()).isEqualTo(2);
            assertThat(knight.hasKeyword(Keyword.VIGILANCE)).isTrue();
        }
    }

    @Test
    @DisplayName("Without spell mastery the Knights are destructible")
    void withoutSpellMasteryNoIndestructible() {
        cast(player1);

        doomBlade(player2, knightsOf(player1).getFirst().getId());

        assertThat(knightsOf(player1)).hasSize(3);
    }

    @Test
    @DisplayName("Spell mastery makes creatures you control, including the new Knights, indestructible")
    void spellMasteryGrantsIndestructible() {
        harness.setGraveyard(player1, List.of(new Shock(), new LavaAxe()));
        harness.addToBattlefield(player1, new GrizzlyBears());

        cast(player1);

        doomBlade(player2, knightsOf(player1).getFirst().getId());
        doomBlade(player2, permanentOf(player1, "Grizzly Bears").getId());

        assertThat(knightsOf(player1)).hasSize(4);
        assertThat(countOf(player1, "Grizzly Bears")).isEqualTo(1);
    }

    @Test
    @DisplayName("One instant/sorcery card in the graveyard is not enough for spell mastery")
    void oneCardIsNotSpellMastery() {
        harness.setGraveyard(player1, List.of(new Shock()));

        cast(player1);

        doomBlade(player2, knightsOf(player1).getFirst().getId());

        assertThat(knightsOf(player1)).hasSize(3);
    }

    @Test
    @DisplayName("Indestructible wears off at end of turn")
    void indestructibleWearsOff() {
        harness.setGraveyard(player1, List.of(new Shock(), new LavaAxe()));

        cast(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        doomBlade(player2, knightsOf(player1).getFirst().getId());

        assertThat(knightsOf(player1)).hasSize(3);
    }

    @Test
    @DisplayName("Opponent creatures never gain indestructible")
    void opponentCreaturesUnaffected() {
        harness.setGraveyard(player1, List.of(new Shock(), new LavaAxe()));
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast(player1);

        doomBlade(player1, permanentOf(player2, "Grizzly Bears").getId());

        assertThat(countOf(player2, "Grizzly Bears")).isZero();
    }

    private void cast(Player player) {
        harness.setHand(player, List.of(new GideonsPhalanx()));
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 5);
        harness.castInstant(player, 0, (UUID) null);
        harness.passBothPriorities();
    }

    private void doomBlade(Player player, UUID targetId) {
        harness.setHand(player, List.of(new DoomBlade()));
        harness.addMana(player, ManaColor.BLACK, 2);
        harness.castInstant(player, 0, targetId);
        harness.passBothPriorities();
    }

    private List<Permanent> knightsOf(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> "Knight".equals(p.getCard().getName()))
                .toList();
    }

    private Permanent permanentOf(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> name.equals(p.getCard().getName()))
                .findFirst()
                .orElseThrow();
    }

    private long countOf(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> name.equals(p.getCard().getName()))
                .count();
    }
}
