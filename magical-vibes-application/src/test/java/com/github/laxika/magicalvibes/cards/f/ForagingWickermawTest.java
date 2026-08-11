package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ForagingWickermawTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield surveils 1")
    void entersWithSurveil() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new ForagingWickermaw()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("The mana ability adds the chosen color and makes the creature that color")
    void manaAbilityAddsManaAndChangesSourceColor() {
        Permanent wickermaw = addReadyWickermaw(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, wickermaw)).containsExactly(CardColor.RED);
    }

    @Test
    @DisplayName("The mana ability can be activated only once each turn")
    void manaAbilityIsLimitedToOnceEachTurn() {
        addReadyWickermaw(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The color change wears off at end of turn")
    void colorChangeWearsOffAtEndOfTurn() {
        Permanent wickermaw = addReadyWickermaw(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        assertThat(gqs.getEffectiveColors(gd, wickermaw)).containsExactly(CardColor.GREEN);

        wickermaw.resetModifiers();
        gd.expireEndOfTurnFloatingEffects();

        assertThat(gqs.getEffectiveColors(gd, wickermaw)).isEmpty();
    }

    private Permanent addReadyWickermaw(Player player) {
        Permanent wickermaw = new Permanent(new ForagingWickermaw());
        wickermaw.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(wickermaw);
        return wickermaw;
    }
}
