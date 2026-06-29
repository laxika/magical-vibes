package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SeanceTest extends BaseCardTest {

    @Test
    @DisplayName("Has each-upkeep may effect to exile own graveyard creature and create Spirit token copy")
    void hasCorrectStructure() {
        Seance card = new Seance();

        assertThat(card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst()).isInstanceOf(MayEffect.class);

        MayEffect may = (MayEffect) card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst();
        assertThat(may.wrapped()).isInstanceOf(ExileTargetCardFromGraveyardAndCreateTokenCopyEffect.class);

        ExileTargetCardFromGraveyardAndCreateTokenCopyEffect effect =
                (ExileTargetCardFromGraveyardAndCreateTokenCopyEffect) may.wrapped();
        assertThat(effect.filter()).isInstanceOf(CardTypePredicate.class);
        assertThat(effect.ownGraveyardOnly()).isTrue();
        assertThat(effect.additionalSubtypes()).containsExactly(CardSubtype.SPIRIT);
        assertThat(effect.grantHaste()).isFalse();
        assertThat(effect.exileAtEndStep()).isTrue();
    }

    @Test
    @DisplayName("Triggers during controller's upkeep and prompts may ability")
    void triggersDuringControllersUpkeep() {
        harness.addToBattlefield(player1, new Seance());
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Triggers during opponent's upkeep for Séance controller")
    void triggersDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new Seance());
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Accepting may exiles creature from graveyard and creates Spirit token copy")
    void acceptingMayCreatesSpiritTokenCopy() {
        harness.addToBattlefield(player1, new Seance());
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Grizzly Bears"))
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.BEAR, CardSubtype.SPIRIT);
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining may leaves graveyard unchanged and creates no token")
    void decliningMayDoesNothing() {
        harness.addToBattlefield(player1, new Seance());
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken());
    }

    @Test
    @DisplayName("Token is exiled at the beginning of the next end step")
    void tokenExiledAtEndStep() {
        harness.addToBattlefield(player1, new Seance());
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears") && p.getCard().isToken());

        advanceToEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears") && p.getCard().isToken());
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
