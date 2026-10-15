package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CautiousSurvivor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.laxika.magicalvibes.model.ManaColor.BLACK;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DefiledCryptCadaverLab.class, Disentomb.class, Forest.class, CautiousSurvivor.class})
class DefiledCryptCadaverLabTest extends BaseCardTest {

    @Test
    void defiledCryptCreatesOnlyOneHorrorPerTurn() {
        CautiousSurvivor firstCreature = new CautiousSurvivor();
        CautiousSurvivor secondCreature = new CautiousSurvivor();
        harness.setGraveyard(player1, List.of(firstCreature, secondCreature));
        harness.setHand(player1, List.of(new DefiledCryptCadaverLab(), new Disentomb(), new Disentomb()));
        harness.addMana(player1, BLACK, 6);

        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        harness.castAndResolveSorcery(player1, 1, firstCreature.getId());
        harness.passBothPriorities();
        harness.castAndResolveSorcery(player1, 0, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(horrorTokens()).isEqualTo(1);
    }

    @Test
    void cadaverLabReturnsTargetCreatureToHandWithoutHorrorWhileCryptIsLocked() {
        CautiousSurvivor creature = new CautiousSurvivor();
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
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creature);
        assertThat(horrorTokens()).isZero();
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

    @Test
    void unlockingCadaverLabWithCryptUnlockedReturnsCreatureAndCreatesHorror() {
        CautiousSurvivor creature = new CautiousSurvivor();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new DefiledCryptCadaverLab()));
        harness.addMana(player1, BLACK, 5);

        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.unlockRoomDoor(player1, 0, 1);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creature);
        assertThat(horrorTokens()).isEqualTo(1);
        var horror = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Horror"))
                .findFirst().orElseThrow();
        assertThat(horror.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(horror.getCard().hasType(CardType.ENCHANTMENT)).isTrue();
        assertThat(horror.getCard().getPower()).isEqualTo(2);
        assertThat(horror.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    void roomWithBothDoorsLockedDoesNotCreateHorror() {
        CautiousSurvivor creature = new CautiousSurvivor();
        harness.addToBattlefield(player1, new DefiledCryptCadaverLab());
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.addMana(player1, BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(horrorTokens()).isZero();
    }

    @Test
    void cadaverLabCannotReturnOpponentsCreature() {
        CautiousSurvivor creature = new CautiousSurvivor();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new DefiledCryptCadaverLab()));
        harness.addMana(player1, BLACK, 1);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature);
        assertThat(horrorTokens()).isZero();
    }
    private long horrorTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Horror"))
                .filter(permanent -> permanent.getCard().hasType(CardType.ENCHANTMENT))
                .count();
    }
}
