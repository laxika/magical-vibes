package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CityOfBrass;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RithsCharmTest extends BaseCardTest {

    @Test
    @DisplayName("Mode 0 destroys a target nonbasic land")
    void destroysNonbasicLand() {
        harness.addToBattlefield(player2, new CityOfBrass());
        harness.setHand(player1, List.of(new RithsCharm()));
        addManaForCast();

        harness.castInstant(player1, 0, 0, harness.getPermanentId(player2, "City of Brass"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "City of Brass");
    }

    @Test
    @DisplayName("Mode 0 cannot target a basic land")
    void rejectsBasicLandTarget() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new RithsCharm()));
        addManaForCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0,
                harness.getPermanentId(player2, "Mountain")))
                .hasMessageContaining("nonbasic land");
    }

    @Test
    @DisplayName("Mode 1 creates three Saproling tokens")
    void createsSaprolings() {
        harness.setHand(player1, List.of(new RithsCharm()));
        addManaForCast();

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).hasSize(3);
    }

    @Test
    @DisplayName("Mode 2 prevents all damage from the chosen source this turn")
    void preventsAllDamageFromChosenSource() {
        harness.setLife(player1, 20);
        Permanent chosenSource = addReadyGoblin(player2);
        Permanent otherSource = addReadyGoblin(player2);
        harness.setHand(player1, List.of(new RithsCharm()));
        addManaForCast();

        harness.castInstant(player1, 0, 2, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosenSource.getId());

        chosenSource.setAttacking(true);
        otherSource.setAttacking(true);
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    private void addManaForCast() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    private Permanent addReadyGoblin(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = new Permanent(new GoblinPiker());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
