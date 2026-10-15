package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.h.HullbreakerHorror;
import com.github.laxika.magicalvibes.cards.s.SorinTheMirthless;
import com.github.laxika.magicalvibes.cards.w.WanderlightSpirit;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RendingFlame.class, HullbreakerHorror.class, SorinTheMirthless.class, WanderlightSpirit.class})
class RendingFlameTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage to a Spirit and 2 damage to its controller")
    void dealsExtraDamageForSpirit() {
        harness.addToBattlefield(player2, new WanderlightSpirit());
        harness.setHand(player1, List.of(new RendingFlame()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Wanderlight Spirit"));

        harness.assertNotOnBattlefield(player2, "Wanderlight Spirit");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not deal extra damage for a non-Spirit creature")
    void doesNotDealExtraDamageForNonSpirit() {
        harness.addToBattlefield(player2, new HullbreakerHorror());
        harness.setHand(player1, List.of(new RendingFlame()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Hullbreaker Horror"));

        assertThat(findPermanent(player2, "Hullbreaker Horror").getMarkedDamage()).isEqualTo(5);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Deals 5 damage to a planeswalker without the Spirit rider")
    void damagesPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new SorinTheMirthless());
        planeswalker.setCounterCount(CounterType.LOYALTY, 8);
        harness.setHand(player1, List.of(new RendingFlame()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Targeting your own Spirit damages you rather than your opponent")
    void damagesControllerOfOwnSpirit() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new WanderlightSpirit());
        harness.setHand(player1, List.of(new RendingFlame()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, spirit.getId());

        harness.assertInGraveyard(player1, "Wanderlight Spirit");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not damage the controller when the Spirit target leaves before resolution")
    void doesNotDamageControllerWhenTargetLeaves() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player2, new WanderlightSpirit());
        harness.setHand(player1, List.of(new RendingFlame(), new RendingFlame()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, spirit.getId());
        harness.castInstant(player1, 0, spirit.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Wanderlight Spirit");
        harness.assertLife(player2, 18);

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof RendingFlame).hasSize(2);
    }

    @Test
    @DisplayName("Lethal planeswalker damage puts the planeswalker in its owner's graveyard")
    void destroysPlaneswalkerWithFiveOrLessLoyalty() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new SorinTheMirthless());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new RendingFlame()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, planeswalker.getId());

        harness.assertNotOnBattlefield(player2, "Sorin the Mirthless");
        harness.assertInGraveyard(player2, "Sorin the Mirthless");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new RendingFlame()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
