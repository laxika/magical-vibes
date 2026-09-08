package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.InspiringCleric;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GangrenousGoliath.class, InspiringCleric.class, GrizzlyBears.class})
class GangrenousGoliathTest extends BaseCardTest {

    @Test
    @DisplayName("Its graveyard ability returns it to its owner's hand")
    void returnsFromGraveyardToHand() {
        GangrenousGoliath goliath = new GangrenousGoliath();
        harness.setGraveyard(player1, List.of(goliath));
        addClerics(player1, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Gangrenous Goliath");
        harness.assertNotInGraveyard(player1, "Gangrenous Goliath");
    }

    @Test
    @DisplayName("Its graveyard ability taps exactly three Clerics as a cost")
    void tapsExactlyThreeClerics() {
        GangrenousGoliath goliath = new GangrenousGoliath();
        harness.setGraveyard(player1, List.of(goliath));
        addClerics(player1, 4);

        harness.activateGraveyardAbility(player1, 0);
        tapClerics(player1, 3);

        long tappedClerics = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.CLERIC))
                .filter(Permanent::isTapped)
                .count();
        assertThat(tappedClerics).isEqualTo(3);
    }

    @Test
    @DisplayName("It cannot be activated without three untapped Clerics")
    void cannotActivateWithoutThreeUntappedClerics() {
        GangrenousGoliath goliath = new GangrenousGoliath();
        harness.setGraveyard(player1, List.of(goliath));
        addClerics(player1, 2);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tapped Clerics cannot be used to pay its graveyard ability")
    void tappedClericsCannotPay() {
        GangrenousGoliath goliath = new GangrenousGoliath();
        harness.setGraveyard(player1, List.of(goliath));
        addClerics(player1, 3);

        gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.CLERIC))
                .findFirst()
                .orElseThrow()
                .tap();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addClerics(Player player, int count) {
        for (int i = 0; i < count; i++) {
            Permanent cleric = new Permanent(new InspiringCleric());
            cleric.setSummoningSick(false);
            gd.playerBattlefields.get(player.getId()).add(cleric);
        }
    }

    private void tapClerics(Player player, int count) {
        gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.CLERIC))
                .filter(p -> !p.isTapped())
                .limit(count)
                .forEach(cleric -> harness.handlePermanentChosen(player, cleric.getId()));
    }
}
