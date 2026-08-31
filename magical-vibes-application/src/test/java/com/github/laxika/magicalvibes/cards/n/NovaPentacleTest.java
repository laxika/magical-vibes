package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NovaPentacle.class, GrizzlyBears.class, ProdigalPyromancer.class})
class NovaPentacleTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent chooses the target creature during activation")
    void opponentChoosesTargetCreature() {
        Permanent pentacle = addReadyPermanent(player1, new NovaPentacle());
        Permanent ownCreature = addReadyCreature(player1);
        Permanent opponentCreature = addReadyCreature(player2);
        addReadyPermanent(player1, new ProdigalPyromancer());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, indexOf(player1, pentacle), null, null);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validPermanentIds()).contains(ownCreature.getId(), opponentCreature.getId());

        harness.handlePermanentChosen(player2, ownCreature.getId());

        assertThat(pentacle.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Redirects the next damage from the chosen source to the creature chosen by the opponent")
    void redirectsNextDamageToOpponentChosenCreature() {
        Permanent pentacle = addReadyPermanent(player1, new NovaPentacle());
        Permanent pyromancer = addReadyPermanent(player1, new ProdigalPyromancer());
        Permanent target = addReadyCreature(player1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, indexOf(player1, pentacle), null, null);
        harness.handlePermanentChosen(player2, target.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, pyromancer.getId());

        harness.activateAbility(player1, indexOf(player1, pyromancer), null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyCreature(Player player) {
        return addReadyPermanent(player, new GrizzlyBears());
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
