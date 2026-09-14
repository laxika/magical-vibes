package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MizzixsMastery.class, CounselOfTheSoratami.class, GrizzlyBears.class})
class MizzixsMasteryTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a targeted instant or sorcery and offers a free copy")
    void castsTargetedCopyForFree() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(counsel));
        harness.setHand(player1, List.of(new MizzixsMastery()));
        addNormalMana();

        harness.castSorcery(player1, 0, List.of(counsel.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Counsel of the Soratami", "Mizzix's Mastery");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Overload exiles every instant and sorcery in your graveyard and offers each copy")
    void overloadsForAllOwnGraveyardSpells() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        GrizzlyBears creature = new GrizzlyBears();
        CounselOfTheSoratami opponentCounsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(counsel, creature));
        harness.setGraveyard(player2, List.of(opponentCounsel));
        harness.setHand(player1, List.of(new MizzixsMastery()));
        addOverloadMana();

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCounsel);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Counsel of the Soratami", "Mizzix's Mastery");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot target a card outside your graveyard or a non-spell card")
    void rejectsIllegalTarget() {
        CounselOfTheSoratami opponentCounsel = new CounselOfTheSoratami();
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(opponentCounsel));
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new MizzixsMastery()));
        addNormalMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(opponentCounsel.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addNormalMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void addOverloadMana() {
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
