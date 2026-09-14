package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.PardicLancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChainersEdict.class, PardicLancer.class})
class ChainersEdictTest extends BaseCardTest {

    @Test
    @DisplayName("Target player sacrifices a creature")
    void targetPlayerSacrificesCreature() {
        harness.addToBattlefield(player2, new PardicLancer());
        harness.setHand(player1, List.of(new ChainersEdict()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertNotOnBattlefield(player2, "Pardic Lancer");
        harness.assertInGraveyard(player2, "Pardic Lancer");
        harness.assertInGraveyard(player1, "Chainer's Edict");
    }

    @Test
    @DisplayName("Target player chooses which creature to sacrifice")
    void targetPlayerChoosesCreatureToSacrifice() {
        Permanent firstLancer = harness.addToBattlefieldAndReturn(player2, new PardicLancer());
        Permanent secondLancer = harness.addToBattlefieldAndReturn(player2, new PardicLancer());
        harness.setHand(player1, List.of(new ChainersEdict()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactlyInAnyOrder(firstLancer.getId(), secondLancer.getId());

        harness.handlePermanentChosen(player2, secondLancer.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(firstLancer).doesNotContain(secondLancer);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(secondLancer.getCard());
    }

    @Test
    @DisplayName("The caster may be the target player")
    void casterMayBeTargetPlayer() {
        harness.addToBattlefield(player1, new PardicLancer());
        harness.setHand(player1, List.of(new ChainersEdict()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertNotOnBattlefield(player1, "Pardic Lancer");
        harness.assertInGraveyard(player1, "Pardic Lancer");
        harness.assertInGraveyard(player1, "Chainer's Edict");
    }

    @Test
    @DisplayName("Flashback sacrifices a creature and exiles Chainer's Edict")
    void flashbackSacrificesCreatureAndExilesSpell() {
        harness.addToBattlefield(player2, new PardicLancer());
        harness.setGraveyard(player1, List.of(new ChainersEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveFlashback(player1, 0, player2.getId());

        harness.assertNotOnBattlefield(player2, "Pardic Lancer");
        harness.assertInGraveyard(player2, "Pardic Lancer");
        harness.assertNotInGraveyard(player1, "Chainer's Edict");
        org.assertj.core.api.Assertions.assertThat(harness.getGameData().getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Chainer's Edict"));
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        var creature = harness.addToBattlefieldAndReturn(player2, new PardicLancer());
        harness.setHand(player1, List.of(new ChainersEdict()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
