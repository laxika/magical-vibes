package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.cards.f.FerociousTigorilla;
import com.github.laxika.magicalvibes.cards.h.HamperingSnare;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BladeBanish.class, FerociousTigorilla.class, AlmightyBrushwagg.class, Plains.class, HamperingSnare.class})
class BladeBanishTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles target creature with power 4 or greater")
    void exilesHighPowerCreature() {
        Permanent target = addCreatureReady(player2, new FerociousTigorilla());
        giveBladeBanish();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Ferocious Tigorilla");
        harness.assertNotInGraveyard(player2, "Ferocious Tigorilla");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Cannot target a creature with power less than 4")
    void cannotTargetLowPowerCreature() {
        Permanent target = addCreatureReady(player2, new AlmightyBrushwagg());
        giveBladeBanish();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());
        giveBladeBanish();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
    }

    @Test
    void canExileOwnCreature() {
        Permanent target = addCreatureReady(player1, new FerociousTigorilla());
        giveBladeBanish();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Ferocious Tigorilla");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
    }

    @Test
    void canTargetCreatureWhoseActivatedAbilityRaisesPowerToFour() {
        Permanent target = addCreatureReady(player2, new AlmightyBrushwagg());
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        giveBladeBanish();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Almighty Brushwagg");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
    }

    @Test
    void doesNotExileCreatureWhosePowerFallsBelowFourBeforeResolution() {
        Permanent target = addCreatureReady(player2, new FerociousTigorilla());
        giveBladeBanish();
        harness.castInstant(player1, 0, target.getId());
        harness.setHand(player1, List.of(new HamperingSnare()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Ferocious Tigorilla");
        harness.assertInGraveyard(player1, "Blade Banish");
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
    }

    private void giveBladeBanish() {
        harness.setHand(player1, List.of(new BladeBanish()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
