package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArchwayCommonsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        playArchwayCommons();

        assertThat(findCommons(player1)).isNotNull();
        assertThat(findCommons(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying {1} keeps Archway Commons on the battlefield")
    void payingKeepsIt() {
        playArchwayCommons();
        resolveEnterTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findCommons(player1)).isNotNull();
        harness.assertNotInGraveyard(player1, "Archway Commons");
    }

    @Test
    @DisplayName("Declining to pay {1} sacrifices Archway Commons")
    void decliningSacrificesIt() {
        playArchwayCommons();
        resolveEnterTrigger();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(findCommons(player1)).isNull();
        harness.assertInGraveyard(player1, "Archway Commons");
    }

    @Test
    @DisplayName("Tap ability adds one mana of the chosen color")
    void tapAddsChosenColorMana() {
        Permanent commons = addCommonsReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(commons.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void playArchwayCommons() {
        harness.setHand(player1, List.of(new ArchwayCommons()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private void resolveEnterTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addCommonsReady(Player player) {
        Permanent commons = new Permanent(new ArchwayCommons());
        commons.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(commons);
        return commons;
    }

    private Permanent findCommons(Player player) {
        return findPermanents(player, "Archway Commons").stream().findFirst().orElse(null);
    }
}
