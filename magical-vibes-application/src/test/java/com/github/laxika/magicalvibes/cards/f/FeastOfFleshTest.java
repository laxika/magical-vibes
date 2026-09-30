package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BorealCentaur;
import com.github.laxika.magicalvibes.cards.b.BorealDruid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FeastOfFlesh.class, BorealCentaur.class, BorealDruid.class})
class FeastOfFleshTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage and gains 1 life with no Feast of Flesh in the graveyards")
    void dealsBaseDamageAndGainsBaseLife() {
        harness.addToBattlefield(player2, new BorealCentaur());
        harness.setHand(player1, List.of(new FeastOfFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Boreal Centaur"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Boreal Centaur");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Counts Feast of Flesh cards in all graveyards for damage and life gain")
    void countsCopiesInAllGraveyards() {
        gd.playerGraveyards.get(player1.getId()).add(new FeastOfFlesh());
        gd.playerGraveyards.get(player2.getId()).add(new FeastOfFlesh());
        harness.addToBattlefield(player2, new BorealCentaur());
        harness.setHand(player1, List.of(new FeastOfFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Boreal Centaur"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Boreal Centaur");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Does not count other cards in graveyards")
    void ignoresOtherCards() {
        gd.playerGraveyards.get(player1.getId()).add(new BorealDruid());
        harness.addToBattlefield(player2, new BorealCentaur());
        harness.setHand(player1, List.of(new FeastOfFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Boreal Centaur"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Boreal Centaur");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("The resolving Feast of Flesh does not count itself")
    void resolvingCopyDoesNotCountItself() {
        harness.addToBattlefield(player2, new BorealCentaur());
        harness.setHand(player1, List.of(new FeastOfFlesh(), new FeastOfFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Boreal Centaur"));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Boreal Centaur");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Boreal Centaur"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Boreal Centaur");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }
}
