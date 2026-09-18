package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlamekinVillage.class, AirElemental.class, GrizzlyBears.class})
class FlamekinVillageTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you have no Elemental card in hand")
    void entersTappedWithoutElemental() {
        playLand(new GrizzlyBears());

        assertThat(findPermanent(player1, "Flamekin Village").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Revealing an Elemental lets it enter untapped")
    void entersUntappedWhenRevealingElemental() {
        playLand(new AirElemental());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(player1, "Flamekin Village").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining to reveal makes it enter tapped")
    void entersTappedWhenDecliningToReveal() {
        playLand(new AirElemental());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Flamekin Village").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping produces one red mana")
    void tappingProducesRedMana() {
        addReadyLand();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("The haste ability grants haste to a target creature")
    void grantsHasteToTargetCreature() {
        addReadyLand();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The haste ability cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addReadyLand();
        Permanent otherLand = harness.addToBattlefieldAndReturn(player1, new FlamekinVillage());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, otherLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void playLand(Card cardInHand) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new FlamekinVillage(), cardInHand));
        harness.playLand(player1, 0);
    }

    private Permanent addReadyLand() {
        Permanent land = new Permanent(new FlamekinVillage());
        land.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(land);
        return land;
    }
}
