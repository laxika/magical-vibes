package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JackOLantern.class, GrizzlyBears.class, Forest.class})
class JackOLanternTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice ability exiles a graveyard card and draws a card")
    void sacrificeAbilityExilesAndDraws() {
        Card target = new GrizzlyBears();
        Card draw = new Forest();
        harness.addToBattlefield(player1, new JackOLantern());
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player1, List.of(draw));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Jack-o'-Lantern");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
    }

    @Test
    @DisplayName("Sacrifice ability may choose no graveyard card and still draws")
    void sacrificeAbilityAllowsNoTarget() {
        harness.addToBattlefield(player1, new JackOLantern());
        Card draw = new Forest();
        harness.setLibrary(player1, List.of(draw));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Jack-o'-Lantern");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
    }

    @Test
    @DisplayName("Graveyard ability exiles itself and adds a chosen mana")
    void graveyardAbilityAddsChosenMana() {
        JackOLantern lantern = new JackOLantern();
        harness.setGraveyard(player1, List.of(lantern));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(lantern);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrifice ability requires a graveyard card target when one is supplied")
    void sacrificeAbilityRejectsMissingTarget() {
        harness.addToBattlefield(player1, new JackOLantern());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        Card target = new GrizzlyBears();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
