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

        harness.castAndResolveSorcery(player1, 0, List.of(counsel.getId()));
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

    @Test
    @DisplayName("Declining the copy leaves the original exiled and draws no cards")
    void declinesTargetedCopy() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(counsel));
        harness.setHand(player1, List.of(new MizzixsMastery()));
        addNormalMana();

        harness.castAndResolveSorcery(player1, 0, List.of(counsel.getId()));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Counsel of the Soratami", "Mizzix's Mastery");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Overload casts all copies without paying their mana costs")
    void overloadCastsMultipleCopies() {
        CounselOfTheSoratami first = new CounselOfTheSoratami();
        CounselOfTheSoratami second = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new MizzixsMastery()));
        addOverloadMana();

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Counsel of the Soratami", "Counsel of the Soratami",
                        "Mizzix's Mastery");
    }

    @Test
    @DisplayName("Overload allows declining one copy and casting another")
    void overloadDeclinesOneCopyIndependently() {
        harness.setGraveyard(player1,
                List.of(new CounselOfTheSoratami(), new CounselOfTheSoratami()));
        harness.setHand(player1, List.of(new MizzixsMastery()));
        addOverloadMana();

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Counsel of the Soratami", "Counsel of the Soratami",
                        "Mizzix's Mastery");
    }

    @Test
    @DisplayName("Overload needs no target and exiles Mastery with an empty graveyard")
    void overloadsWithEmptyGraveyard() {
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new MizzixsMastery()));
        addOverloadMana();

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName).containsExactly("Mizzix's Mastery");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An illegal target prevents all effects, including Mastery's self-exile")
    void missingTargetMakesMasteryGoToGraveyard() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(counsel));
        harness.setHand(player1, List.of(new MizzixsMastery()));
        addNormalMana();

        harness.castSorcery(player1, 0, List.of(counsel.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mizzix's Mastery");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
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
