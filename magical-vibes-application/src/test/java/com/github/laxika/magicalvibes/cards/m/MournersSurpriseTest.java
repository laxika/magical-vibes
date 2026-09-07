package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MournersSurprise.class, GrizzlyBears.class})
class MournersSurpriseTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to one creature card and creates a Mercenary token")
    void returnsCreatureAndCreatesMercenary() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new MournersSurprise()));
        castMournersSurprise(creature.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Mourner's Surprise"));
        assertThat(findPermanents(player1, "Mercenary")).hasSize(1);
    }

    @Test
    @DisplayName("Creates a Mercenary token when no graveyard target is chosen")
    void createsTokenWithoutGraveyardTarget() {
        harness.setHand(player1, List.of(new MournersSurprise()));
        castMournersSurprise();

        assertThat(findPermanents(player1, "Mercenary")).hasSize(1);
        harness.assertInGraveyard(player1, "Mourner's Surprise");
    }

    @Test
    @DisplayName("The created Mercenary can boost a creature you control")
    void mercenaryBoostsCreatureYouControl() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MournersSurprise()));
        castMournersSurprise();

        Permanent mercenary = findPermanent(player1, "Mercenary");
        mercenary.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int mercenaryIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);

        harness.activateAbility(player1, mercenaryIndex, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(mercenary.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature card in the graveyard")
    void cannotTargetNoncreatureCard() {
        Card noncreature = new MournersSurprise();
        harness.setGraveyard(player1, List.of(noncreature));
        harness.setHand(player1, List.of(new MournersSurprise()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castMournersSurprise() {
        castMournersSurprise(null);
    }

    private void castMournersSurprise(java.util.UUID graveyardTargetId) {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        if (graveyardTargetId == null) {
            harness.castSorcery(player1, 0, 0);
        } else {
            harness.castSorcery(player1, 0, graveyardTargetId);
        }
        harness.passBothPriorities();
    }
}
