package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FeralProwler;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HostileDesert.class, Forest.class, FeralProwler.class})
class HostileDesertTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping produces colorless mana")
    void tappingProducesColorlessMana() {
        Permanent desert = addDesertReady(player1);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(desert);

        gs.tapPermanent(gd, player1, index);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability exiles a land from the graveyard and animates into a 3/4 Elemental")
    void animatesIntoElemental() {
        Permanent desert = addDesertReady(player1);
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
        assertThat(gqs.isCreature(gd, desert)).isTrue();
        assertThat(gqs.getEffectivePower(gd, desert)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, desert)).isEqualTo(4);
        assertThat(desert.getTransientSubtypes()).contains(CardSubtype.ELEMENTAL);
        assertThat(gqs.isLand(gd, desert)).isTrue();
    }

    @Test
    @DisplayName("Animation resets at end of turn")
    void animationResetsAtEndOfTurn() {
        Permanent desert = addDesertReady(player1);
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, desert)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(desert.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, desert)).isFalse();
        assertThat(desert.getTransientSubtypes()).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without a land card in graveyard")
    void cannotActivateWithoutLandInGraveyard() {
        addDesertReady(player1);
        harness.setGraveyard(player1, List.of(new FeralProwler()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exiling and paying mana happen before animation resolves")
    void paysCostsBeforeResolution() {
        Permanent desert = addDesertReady(player1);
        Forest land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(land);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gqs.isCreature(gd, desert)).isFalse();
        assertThat(desert.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, desert)).isTrue();
    }

    @Test
    @DisplayName("Animation can be activated while tapped and affects only its source")
    void animatesTappedSourceOnly() {
        Permanent desert = addDesertReady(player1);
        Permanent otherDesert = addDesertReady(player1);
        desert.tap();
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, desert)).isTrue();
        assertThat(desert.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, otherDesert)).isFalse();
    }

    @Test
    @DisplayName("An animated Desert retains its mana ability")
    void animatedDesertStillProducesMana() {
        Permanent desert = addDesertReady(player1);
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.tapPermanent(player1, 0);

        assertThat(desert.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, desert)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's graveyard cannot supply the land cost")
    void cannotExileOpponentsLand() {
        addDesertReady(player1);
        harness.setGraveyard(player1, List.of());
        Forest land = new Forest();
        harness.setGraveyard(player2, List.of(land));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(land);
    }

    private Permanent addDesertReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new HostileDesert());
        perm.setSummoningSick(false);
        return perm;
    }
}
