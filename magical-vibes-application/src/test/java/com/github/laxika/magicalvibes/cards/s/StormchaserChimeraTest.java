package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StormchaserChimeraTest extends BaseCardTest {

    @Test
    @DisplayName("Scrying keeps a high-mana-value card on top and boosts by its mana value")
    void keepsTopCardAndBoostsByItsManaValue() {
        Permanent chimera = addReadyChimera(player1);
        Card topCard = new AirElemental();
        harness.setLibrary(player1, List.of(topCard, new GrizzlyBears()));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(chimera.getEffectivePower()).isEqualTo(7);
        assertThat(chimera.getPowerModifier()).isEqualTo(5);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    @DisplayName("Putting the scried card on the bottom makes the next top card determine the boost")
    void bottomedCardIsNotUsedForBoost() {
        Permanent chimera = addReadyChimera(player1);
        Card scriedCard = new AirElemental();
        Card revealedCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(scriedCard, revealedCard));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(chimera.getEffectivePower()).isEqualTo(4);
        assertThat(chimera.getPowerModifier()).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(revealedCard);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private Permanent addReadyChimera(Player player) {
        Permanent chimera = new Permanent(new StormchaserChimera());
        chimera.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(chimera);
        return chimera;
    }
}
