package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.Agility;
import com.github.laxika.magicalvibes.cards.f.FemerefKnight;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.Regeneration;
import com.github.laxika.magicalvibes.cards.u.UrborgPanther;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CinderCloud.class, Agility.class, FemerefKnight.class, Plains.class, Regeneration.class,
        UrborgPanther.class})
class CinderCloudTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a white creature and deals damage equal to its power to its controller")
    void whiteCreatureDealsPowerDamage() {
        harness.addToBattlefield(player2, new FemerefKnight());
        harness.setHand(player1, List.of(new CinderCloud()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Femeref Knight");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertInGraveyard(player2, "Femeref Knight");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Destroys a non-white creature without dealing damage")
    void nonWhiteCreatureDealsNoDamage() {
        harness.addToBattlefield(player2, new UrborgPanther());
        harness.setHand(player1, List.of(new CinderCloud()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Urborg Panther");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertInGraveyard(player2, "Urborg Panther");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Deals no damage when the white creature survives destruction")
    void regeneratedWhiteCreatureTakesNoDamage() {
        harness.addToBattlefield(player2, new FemerefKnight());
        harness.setHand(player1, List.of(new Regeneration(), new CinderCloud()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID targetId = harness.getPermanentId(player2, "Femeref Knight");
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 5);
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertOnBattlefield(player2, "Femeref Knight");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Deals damage equal to a white creature's modified power")
    void whiteCreatureDealsModifiedPowerDamage() {
        harness.addToBattlefield(player2, new FemerefKnight());
        harness.setHand(player1, List.of(new Agility(), new CinderCloud()));
        harness.addMana(player1, ManaColor.RED, 7);

        UUID targetId = harness.getPermanentId(player2, "Femeref Knight");
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertInGraveyard(player2, "Femeref Knight");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new UrborgPanther());
        harness.setHand(player1, List.of(new CinderCloud()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Plains");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
