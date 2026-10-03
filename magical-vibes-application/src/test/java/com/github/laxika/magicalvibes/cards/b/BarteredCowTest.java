package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Sift;
import com.github.laxika.magicalvibes.cards.t.ThrillOfPossibility;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BarteredCow.class, Forest.class, LightningBolt.class, Sift.class, ThrillOfPossibility.class})
class BarteredCowTest extends BaseCardTest {

    @Test
    @DisplayName("When Bartered Cow dies, it creates a Food token")
    void deathCreatesFoodToken() {
        harness.addToBattlefield(player1, new BarteredCow());
        UUID cowId = harness.getPermanentId(player1, "Bartered Cow");
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, cowId);
        resolveAllTriggers();

        Permanent food = findPermanent(player1, "Food");
        assertThat(food.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(food.getCard().getSubtypes()).contains(CardSubtype.FOOD);
        harness.assertInGraveyard(player1, "Bartered Cow");
    }

    @Test
    @DisplayName("When Bartered Cow is discarded, it creates a Food token")
    void discardCreatesFoodToken() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new Sift(), new BarteredCow()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Bartered Cow");
    }

    @Test
    @DisplayName("Food created by Bartered Cow can be sacrificed for 3 life")
    void foodCanBeSacrificedForLife() {
        harness.addToBattlefield(player1, new BarteredCow());
        UUID cowId = harness.getPermanentId(player1, "Bartered Cow");
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, cowId);
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.assertNotOnBattlefield(player1, "Food");
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        harness.assertNotOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Discarding Cow as a spell cost creates Food before the spell resolves")
    void discardAsCostTriggersBeforeDrawSpell() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new ThrillOfPossibility(), new BarteredCow()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstantWithDiscard(player1, 0, null, 1);

        harness.assertInGraveyard(player1, "Bartered Cow");
        assertThat(countPermanents(player1, "Food")).isZero();
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
    }

    @Test
    @DisplayName("Each dying Cow creates its own Food only when its trigger resolves")
    void multipleDeathsCreateSeparateFoodTriggers() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BarteredCow());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BarteredCow());
        first.setMarkedDamage(3);
        second.setMarkedDamage(3);

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Bartered Cow");
        assertThat(countPermanents(player1, "Food")).isZero();
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Food")).isEqualTo(2);
        assertThat(countPermanents(player2, "Food")).isZero();
    }

    @Test
    @DisplayName("A tapped Food cannot pay its tap cost")
    void tappedFoodCannotBeActivated() {
        Permanent cow = harness.addToBattlefieldAndReturn(player1, new BarteredCow());
        cow.setMarkedDamage(3);
        harness.runStateBasedActions();
        resolveAllTriggers();
        findPermanent(player1, "Food").setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Food requires two mana and is not sacrificed when payment fails")
    void foodCannotBeActivatedWithOnlyOneMana() {
        Permanent cow = harness.addToBattlefieldAndReturn(player1, new BarteredCow());
        cow.setMarkedDamage(3);
        harness.runStateBasedActions();
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        Permanent food = findPermanent(player1, "Food");
        assertThat(food.isTapped()).isFalse();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }
}
