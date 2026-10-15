package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Drekavac;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RakdosGuildmage.class, Drekavac.class, RakdosSignet.class})
class RakdosGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("Black ability requires discarding a card and gives a creature -2/-2")
    void blackAbilityDebuffsTargetCreature() {
        addCreatureReady(player1, new RakdosGuildmage());
        Permanent target = addCreatureReady(player2, new RakdosGuildmage());
        harness.setHand(player1, List.of(new Drekavac()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);

        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Drekavac");
        harness.assertInGraveyard(player2, "Rakdos Guildmage");
    }

    @Test
    @DisplayName("Black ability's -2/-2 lasts only until end of turn")
    void blackAbilityDebuffExpiresAtEndOfTurn() {
        addCreatureReady(player1, new RakdosGuildmage());
        Permanent target = addCreatureReady(player2, new Drekavac());
        harness.setHand(player1, List.of(new Drekavac()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Black ability cannot be activated without a card to discard")
    void blackAbilityRequiresCardInHand() {
        addCreatureReady(player1, new RakdosGuildmage());
        Permanent target = addCreatureReady(player2, new RakdosGuildmage());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("discard");
    }

    @Test
    @DisplayName("Black ability cannot target a noncreature permanent")
    void blackAbilityRejectsNoncreatureTarget() {
        addCreatureReady(player1, new RakdosGuildmage());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RakdosSignet());
        harness.setHand(player1, List.of(new Drekavac()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Red ability creates a hasty 2/1 Goblin that is exiled at the next end step")
    void redAbilityCreatesAndExilesGoblinToken() {
        addCreatureReady(player1, new RakdosGuildmage());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.GOBLIN);
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(token.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
    }

    @Test
    @DisplayName("Red ability exiles the token at the next end step even on the opponent's turn")
    void redAbilityExilesTokenAtOpponentsNextEndStep() {
        addCreatureReady(player1, new RakdosGuildmage());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
    }

    @Test
    @DisplayName("A token created during an end step survives until the following end step")
    void tokenCreatedDuringEndStepWaitsForFollowingEndStep() {
        addCreatureReady(player1, new RakdosGuildmage());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);

        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
    }
}
