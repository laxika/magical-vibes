package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheDominionBracelet.class, GrizzlyBears.class})
class TheDominionBraceletTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsPlusOnePlusOne() {
        Permanent creature = addReadyPermanent(player1, new GrizzlyBears());
        Permanent bracelet = addReadyPermanent(player1, new TheDominionBracelet());
        bracelet.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void abilityUsesEquippedPowerForReductionAndExilesBracelet() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent creature = addReadyPermanent(player1, new GrizzlyBears());
        Permanent bracelet = addReadyPermanent(player1, new TheDominionBracelet());
        bracelet.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 12);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.pendingTurnControl).containsEntry(player2.getId(), player1.getId());
        assertThat(gd.findExiledCard(bracelet.getCard().getId())).isNotNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void abilityCanTargetOnlyAnOpponent() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent creature = addReadyPermanent(player1, new GrizzlyBears());
        Permanent bracelet = addReadyPermanent(player1, new TheDominionBracelet());
        bracelet.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 12);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
