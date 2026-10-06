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
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, false);
        }
        harness.castFromGraveyardTargeting(player1, 0, target.getId());
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
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, false);
        }
        harness.castFromGraveyardTargeting(player1, 0, target.getId());
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
    @DisplayName("The targeted instant can be cast later in the turn without paying its mana cost")
    void targetedInstantCanBeCastLater() {
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

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(lastGasp);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(sins.getId());

        harness.castFromGraveyardTargeting(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grayscaled Gharial");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(sins.getId(), lastGasp.getId());
    }

    @Test
    @DisplayName("Resolution grants permission without prompting to cast the targeted card")
    void resolutionDoesNotPromptToCast() {
        LastGasp lastGasp = new LastGasp();
        harness.addToBattlefield(player2, new GrayscaledGharial());
        harness.setGraveyard(player1, List.of(lastGasp));
        harness.setHand(player1, List.of(new SinsOfThePast()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, lastGasp.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(lastGasp);
    }

    @Test
    @DisplayName("A targeted sorcery can be cast for free after Sins of the Past resolves")
    void canCastTargetedSorceryLater() {
        SinsOfThePast first = new SinsOfThePast();
        SinsOfThePast second = new SinsOfThePast();
        LastGasp lastGasp = new LastGasp();
        harness.setGraveyard(player1, List.of(second, lastGasp));
        harness.setHand(player1, List.of(first));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, second.getId());
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, false);
        }
        harness.castFromGraveyardTargeting(player1, 0, lastGasp.getId());
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, false);
        }

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId).containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(lastGasp);
    }

    @Test
    @DisplayName("Sins of the Past goes to the graveyard if its only target becomes illegal")
    void illegalTargetPreventsSelfExile() {
        LastGasp lastGasp = new LastGasp();
        SinsOfThePast sins = new SinsOfThePast();
        harness.setGraveyard(player1, List.of(lastGasp));
        harness.setHand(player1, List.of(sins));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, lastGasp.getId());
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(lastGasp));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(sins);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(lastGasp);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The targeted spell can wait until it has a legal target later in the turn")
    void canWaitForALegalTarget() {
        LastGasp lastGasp = new LastGasp();
        harness.setGraveyard(player1, List.of(lastGasp));
        harness.setHand(player1, List.of(new SinsOfThePast()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, lastGasp.getId());
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, false);
        }

        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrayscaledGharial());
        harness.castFromGraveyardTargeting(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grayscaled Gharial");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId).contains(lastGasp.getId());
    }
}
