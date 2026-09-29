package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.effect.EnsnaredByTheMaraVillainousChoiceEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EnsnaredByTheMara.class, Forest.class, GrizzlyBears.class})
class EnsnaredByTheMaraTest extends BaseCardTest {

    @Test
    void opponentCanChooseFreeCastFromTheirLibrary() {
        Forest exiledLand = new Forest();
        GrizzlyBears exiledSpell = new GrizzlyBears();
        harness.setLibrary(player2, List.of(exiledLand, exiledSpell));

        cast();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleListChoice(player2, EnsnaredByTheMaraVillainousChoiceEffect.CAST_OPTION);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(exiledLand);
    }

    @Test
    void decliningFreeCastLeavesTheNonlandCardExiled() {
        GrizzlyBears exiledSpell = new GrizzlyBears();
        harness.setLibrary(player2, List.of(exiledSpell));

        cast();
        harness.handleListChoice(player2, EnsnaredByTheMaraVillainousChoiceEffect.CAST_OPTION);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(exiledSpell);
        harness.assertLife(player2, 20);
    }

    @Test
    void opponentCanChooseExileFourAndTakeTotalManaValueDamage() {
        List<com.github.laxika.magicalvibes.model.Card> exiledCards = List.of(
                new GrizzlyBears(), new Forest(), new GrizzlyBears(), new Forest());
        harness.setLibrary(player2, exiledCards);

        cast();
        harness.handleListChoice(player2, EnsnaredByTheMaraVillainousChoiceEffect.DAMAGE_OPTION);

        harness.assertLife(player2, 16);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyElementsOf(exiledCards);
    }

    private void cast() {
        harness.setHand(player1, List.of(new EnsnaredByTheMara()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
