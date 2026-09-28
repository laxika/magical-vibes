package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Convolute;
import com.github.laxika.magicalvibes.cards.g.GrayscaledGharial;
import com.github.laxika.magicalvibes.cards.l.LastGasp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SinsOfThePast.class, LastGasp.class, GrayscaledGharial.class, Convolute.class})
class SinsOfThePastTest extends BaseCardTest {

    @Test
    @DisplayName("Casts a targeted instant from your graveyard for free and exiles both spells")
    void castsTargetedInstantForFreeAndExilesBothSpells() {
        LastGasp lastGasp = new LastGasp();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrayscaledGharial());
        SinsOfThePast sins = new SinsOfThePast();
        harness.setGraveyard(player1, List.of(lastGasp));
        harness.setHand(player1, List.of(sins));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, lastGasp.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(lastGasp.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(sins.getId(), lastGasp.getId());
    }

    @Test
    @DisplayName("Cannot target a creature card in your graveyard")
    void cannotTargetCreatureCard() {
        Card creature = new GrayscaledGharial();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new SinsOfThePast()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card lastGasp = new LastGasp();
        harness.setGraveyard(player2, List.of(lastGasp));
        harness.setHand(player1, List.of(new SinsOfThePast()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, lastGasp.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exiles the free-cast spell when it is countered")
    void exilesFreeCastSpellWhenCountered() {
        LastGasp lastGasp = new LastGasp();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrayscaledGharial());
        SinsOfThePast sins = new SinsOfThePast();
        Convolute convolute = new Convolute();
        harness.setGraveyard(player1, List.of(lastGasp));
        harness.setHand(player1, List.of(sins));
        harness.setHand(player2, List.of(convolute));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, lastGasp.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, lastGasp.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(lastGasp.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(sins.getId(), lastGasp.getId());
    }

    @Test
    @DisplayName("Declining the cast does not grant permission to cast the targeted spell later")
    void decliningCastDoesNotGrantLaterPermission() {
        LastGasp lastGasp = new LastGasp();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrayscaledGharial());
        SinsOfThePast sins = new SinsOfThePast();
        harness.setGraveyard(player1, List.of(lastGasp));
        harness.setHand(player1, List.of(sins));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, lastGasp.getId());
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, false);
        }

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card cannot be cast from graveyard");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(lastGasp);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(sins.getId());
    }
}
