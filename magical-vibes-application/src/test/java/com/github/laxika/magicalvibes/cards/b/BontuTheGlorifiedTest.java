package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BontuTheGlorifiedTest extends BaseCardTest {

    // ===== Attack restriction: a creature must have died under your control this turn =====

    @Test
    @DisplayName("Can attack when a creature died under your control this turn")
    void canAttackWhenCreatureDiedUnderYourControl() {
        harness.setLife(player2, 20);
        addReadyBontu(player1);
        gd.creatureDeathCountThisTurn.put(player1.getId(), 1);

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("Cannot attack when no creature died this turn")
    void cannotAttackWhenNoCreatureDied() {
        addReadyBontu(player1);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot attack when only an opponent's creature died this turn")
    void cannotAttackWhenOnlyOpponentCreatureDied() {
        addReadyBontu(player1);
        gd.creatureDeathCountThisTurn.put(player2.getId(), 1);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Activated ability: sacrifice another creature to scry and drain =====

    @Test
    @DisplayName("Sacrificing another creature scries, drains each opponent, and gains life")
    void abilityScriesDrainsAndGains() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addReadyBontu(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        setDeck(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Scry 1 is offered; keep the card on top to finish resolution
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        // The other creature was sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Opponent loses 1, controller gains 1
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Cannot activate when Bontu is the only creature (can't sacrifice itself)")
    void cannotSacrificeItself() {
        addReadyBontu(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addReadyBontu(Player player) {
        Permanent perm = new Permanent(new BontuTheGlorified());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
