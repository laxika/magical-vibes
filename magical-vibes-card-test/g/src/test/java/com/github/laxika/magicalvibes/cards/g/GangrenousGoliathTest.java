package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DaruHealer;
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

@CardUsed({GangrenousGoliath.class, DaruHealer.class, GlorySeeker.class})
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

        addCreatureReady(player1, new GlorySeeker());

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("It cannot use Clerics controlled by another player")
    void cannotUseOpponentsClerics() {
        GangrenousGoliath goliath = new GangrenousGoliath();
        harness.setGraveyard(player1, List.of(goliath));
        addClerics(player1, 2);
        addClerics(player2, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Its graveyard ability returns only Gangrenous Goliath")
    void returnsOnlyItsSourceFromGraveyard() {
        GangrenousGoliath goliath = new GangrenousGoliath();
        GlorySeeker otherCard = new GlorySeeker();
        harness.setGraveyard(player1, List.of(goliath, otherCard));
        addClerics(player1, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Gangrenous Goliath");
        harness.assertInGraveyard(player1, "Glory Seeker");
    }

    @Test
    @DisplayName("Tapped Clerics cannot be used to pay its graveyard ability")
    void tappedClericsCannotPay() {
        GangrenousGoliath goliath = new GangrenousGoliath();
        harness.setGraveyard(player1, List.of(goliath));
        addClerics(player1, 3);

        findPermanent(player1, "Daru Healer").tap();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Summoning-sick Clerics can pay its graveyard ability")
    void summoningSickClericsCanPay() {
        harness.setGraveyard(player1, List.of(new GangrenousGoliath()));
        for (int i = 0; i < 3; i++) {
            Permanent cleric = harness.addToBattlefieldAndReturn(player1, new DaruHealer());
            cleric.setSummoningSick(true);
        }

        harness.activateGraveyardAbility(player1, 0);
        assertThat(findPermanents(player1, "Daru Healer")).allMatch(Permanent::isTapped);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Gangrenous Goliath");
        harness.assertNotInGraveyard(player1, "Gangrenous Goliath");
    }

    @Test
    @DisplayName("Its graveyard ability returns only the activated copy")
    void returnsOnlyActivatedCopy() {
        GangrenousGoliath first = new GangrenousGoliath();
        GangrenousGoliath second = new GangrenousGoliath();
        harness.setGraveyard(player1, List.of(first, second));
        addClerics(player1, 3);

        harness.activateGraveyardAbility(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(second).doesNotContain(first);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
    }

    @Test
    @DisplayName("Removing its source before resolution does not return another copy or refund the cost")
    void sourceAbsentAtResolution() {
        GangrenousGoliath source = new GangrenousGoliath();
        GangrenousGoliath other = new GangrenousGoliath();
        harness.setGraveyard(player1, List.of(source, other));
        addClerics(player1, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.setGraveyard(player1, List.of(other));
        gd.addToExile(player1.getId(), source, null, false);
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Gangrenous Goliath");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        assertThat(gd.findExiledCard(source.getId())).isNotNull();
        assertThat(findPermanents(player1, "Daru Healer")).allMatch(Permanent::isTapped);
    }

    private void addClerics(Player player, int count) {
        for (int i = 0; i < count; i++) {
            addCreatureReady(player, new DaruHealer());
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
