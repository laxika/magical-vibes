package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SamiteMinistrationTest extends BaseCardTest {

    @Test
    void resolvingPromptsForSourceChoice() {
        castSamiteMinistration();
        addReadyCreature(player2, new GoblinPiker());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }

    @Test
    void preventsRedCombatDamageAndGainsThatMuchLife() {
        harness.setLife(player1, 20);
        Permanent source = addReadyCreature(player2, new GoblinPiker());
        castSamiteMinistration();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        source.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 22);
    }

    @Test
    void preventsBlackCombatDamageAndGainsThatMuchLife() {
        harness.setLife(player1, 20);
        Permanent source = addReadyCreature(player2, new BlackKnight());
        castSamiteMinistration();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        source.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 22);
    }

    @Test
    void preventsOtherColorCombatDamageWithoutGainingLife() {
        harness.setLife(player1, 20);
        Permanent source = addReadyCreature(player2, new GrizzlyBears());
        castSamiteMinistration();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        source.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
    }

    private void castSamiteMinistration() {
        harness.setHand(player1, List.of(new SamiteMinistration()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
