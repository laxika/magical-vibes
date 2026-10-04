package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CurseOfBounty.class, GrizzlyBears.class, Island.class})
class CurseOfBountyTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps nonland permanents controlled by the curse controller and attacking opponent")
    void untapsNonlandPermanentsButNotLands() {
        placeCurseOnPlayer(player1, player1);
        Permanent ownCreature = tappedPermanent(player1, new GrizzlyBears());
        Permanent ownLand = tappedPermanent(player1, new Island());
        Permanent opponentSupport = tappedPermanent(player2, new GrizzlyBears());
        Permanent opponentLand = tappedPermanent(player2, new Island());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(2));
        resolveAllTriggers();

        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(ownLand.isTapped()).isTrue();
        assertThat(opponentSupport.isTapped()).isFalse();
        assertThat(opponentLand.isTapped()).isTrue();
        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not trigger when an opponent attacks a different player")
    void doesNotTriggerForAnUnenchantedPlayer() {
        placeCurseOnPlayer(player1, player2);
        Permanent ownCreature = tappedPermanent(player1, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(ownCreature.isTapped()).isTrue();
        assertThat(attacker.isTapped()).isTrue();
    }

    private Permanent tappedPermanent(Player player, Card card) {
        Permanent battlefieldPermanent = harness.addToBattlefieldAndReturn(player, card);
        battlefieldPermanent.tap();
        return battlefieldPermanent;
    }

    private void placeCurseOnPlayer(Player controller, Player enchantedPlayer) {
        Permanent curse = harness.addToBattlefieldAndReturn(controller, new CurseOfBounty());
        curse.setAttachedTo(enchantedPlayer.getId());
    }
}
