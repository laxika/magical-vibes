package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VoldarenEpicure.class})
class VoldarenEpicureTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 1 damage to each opponent and creates a Blood token")
    void etbDamagesOpponentAndCreatesBlood() {
        harness.setHand(player1, List.of(new VoldarenEpicure()));
        harness.addMana(player1, ManaColor.RED, 1);
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());
        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore);

        List<Permanent> bloods = findPermanents(player1, "Blood");
        assertThat(bloods).hasSize(1);
        Permanent blood = bloods.getFirst();
        assertThat(blood.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(blood.getCard().getSubtypes()).contains(CardSubtype.BLOOD);
        assertThat(blood.getCard().isToken()).isTrue();
        assertThat(findPermanents(player2, "Blood")).isEmpty();
    }

    @Test
    void bloodTokenPaysDiscardAndSacrificeBeforeDrawing() {
        harness.setHand(player1, List.of(new VoldarenEpicure()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent blood = findPermanent(player1, "Blood");
        VoldarenEpicure discarded = new VoldarenEpicure();
        VoldarenEpicure drawn = new VoldarenEpicure();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(blood), null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blood);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void entryTriggerResolvesAfterEpicureLeavesBattlefield() {
        harness.setHand(player1, List.of(new VoldarenEpicure()));
        harness.addMana(player1, ManaColor.RED, 1);
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());
        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        assertThat(countPermanents(player1, "Blood")).isZero();
        Permanent epicure = findPermanent(player1, "Voldaren Epicure");
        gd.playerBattlefields.get(player1.getId()).remove(epicure);
        gd.playerGraveyards.get(player1.getId()).add(epicure.getCard());

        resolveAllTriggers();

        harness.assertLife(player2, opponentLifeBefore - 1);
        harness.assertLife(player1, controllerLifeBefore);
        assertThat(countPermanents(player1, "Blood")).isEqualTo(1);
        assertThat(findPermanents(player2, "Blood")).isEmpty();
    }
}
