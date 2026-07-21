package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        assertThat(desert.getCard().getType()).isEqualTo(CardType.LAND);
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
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addDesertReady(Player player) {
        Permanent perm = new Permanent(new HostileDesert());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
