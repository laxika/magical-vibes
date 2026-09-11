package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.laxika.magicalvibes.model.ManaColor.BLACK;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DefiledCryptCadaverLab.class, Disentomb.class, Forest.class, GrizzlyBears.class})
class DefiledCryptCadaverLabTest extends BaseCardTest {

    @Test
    void defiledCryptCreatesOnlyOneHorrorPerTurn() {
        GrizzlyBears firstCreature = new GrizzlyBears();
        GrizzlyBears secondCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstCreature, secondCreature));
        harness.setHand(player1, List.of(new DefiledCryptCadaverLab(), new Disentomb(), new Disentomb()));
        harness.addMana(player1, BLACK, 6);

        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        harness.castSorcery(player1, 1, firstCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castSorcery(player1, 0, secondCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(horrorTokens()).isEqualTo(1);
    }

    @Test
    void cadaverLabReturnsTargetCreatureToHandAndCreatesHorror() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new DefiledCryptCadaverLab()));
        harness.addMana(player1, BLACK, 1);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(horrorTokens()).isEqualTo(1);
    }

    @Test
    void cadaverLabCannotTargetNoncreatureCard() {
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));
        harness.setHand(player1, List.of(new DefiledCryptCadaverLab()));
        harness.addMana(player1, BLACK, 1);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(forest);
        assertThat(horrorTokens()).isZero();
    }

    private long horrorTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Horror"))
                .filter(permanent -> permanent.getCard().hasType(CardType.ENCHANTMENT))
                .count();
    }
}
