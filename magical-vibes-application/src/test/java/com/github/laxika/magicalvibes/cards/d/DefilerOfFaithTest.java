package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Defiler of Faith")
@CardUsed(DefilerOfFaith.class)
class DefilerOfFaithTest extends BaseCardTest {

    @Test
    @DisplayName("paying 2 life reduces a white permanent spell by {W} and creates a Soldier")
    void paysLifeForWhitePermanentSpell() {
        addCreatureReady(player1, new DefilerOfFaith());
        harness.setHand(player1, List.of(new SavannahLions()));
        harness.setLife(player1, 20);

        harness.ensurePriority(player1);
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, List.of(), null, null, false, null, null, null, List.of(), List.of(), false,
                null, null, List.of(), List.of(), null, null, false, true, null);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(soldierTokens(player1)).hasSize(1);
    }

    @Test
    @DisplayName("paying the reduced white mana cost leaves life unchanged and creates a Soldier")
    void paysReducedManaForWhitePermanentSpell() {
        addCreatureReady(player1, new DefilerOfFaith());
        harness.setHand(player1, List.of(new SavannahLions()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(soldierTokens(player1)).hasSize(1);
    }

    @Test
    @DisplayName("nonwhite permanent spells do not receive the cost reduction or trigger")
    void ignoresNonwhitePermanentSpell() {
        addCreatureReady(player1, new DefilerOfFaith());
        GrizzlyBears greenCreature = new GrizzlyBears();
        harness.setHand(player1, List.of(greenCreature));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(soldierTokens(player1)).isEmpty();
    }

    private List<com.github.laxika.magicalvibes.model.Permanent> soldierTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> "Soldier".equals(permanent.getCard().getName()))
                .toList();
    }
}
