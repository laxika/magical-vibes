package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantCrab;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Scuttletide.class, GrizzlyBears.class, GiantCrab.class, Plains.class, Shock.class,
        LeoninScimitar.class, Pacifism.class})
class ScuttletideTest extends BaseCardTest {

    @Test
    @DisplayName("Pays {1} and discards a card to create a 0/3 blue Crab")
    void createsCrabAfterDiscarding() {
        Permanent scuttletide = castScuttletide();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(scuttletide),
                0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent crab = findPermanent(player1, "Crab");
        assertThat(crab.getCard().getPower()).isZero();
        assertThat(crab.getCard().getToughness()).isEqualTo(3);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Delirium gives Crabs you control +1/+1")
    void deliriumBoostsOwnCrabsOnly() {
        harness.setGraveyard(player1, List.of(
                new Plains(), new Shock(), new LeoninScimitar(), new Pacifism()));
        Permanent opponentCrab = harness.addToBattlefieldAndReturn(player2, new GiantCrab());
        int opponentCrabPower = gqs.getEffectivePower(gd, opponentCrab);
        int opponentCrabToughness = gqs.getEffectiveToughness(gd, opponentCrab);
        Permanent scuttletide = castScuttletide();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(scuttletide),
                0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent crab = findPermanent(player1, "Crab");
        assertThat(gqs.getEffectivePower(gd, crab)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, crab)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentCrab)).isEqualTo(opponentCrabPower);
        assertThat(gqs.getEffectiveToughness(gd, opponentCrab)).isEqualTo(opponentCrabToughness);
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardInHand() {
        Permanent scuttletide = castScuttletide();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(scuttletide), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent castScuttletide() {
        harness.setHand(player1, List.of(new Scuttletide()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Scuttletide");
    }
}
