package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SanctumOfEternity.class, GrizzlyBears.class})
class SanctumOfEternityTest extends BaseCardTest {

    @Test
    void addsColorlessMana() {
        harness.addToBattlefield(player1, new SanctumOfEternity());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void returnsOwnedCommanderToHand() {
        Card commanderCard = new GrizzlyBears();
        commanderCard.setOwnerId(player1.getId());
        gd.makeCommander(player1.getId(), commanderCard);
        gd.playerCommandZones.get(player1.getId()).clear();

        Permanent sanctum = harness.addToBattlefieldAndReturn(player1, new SanctumOfEternity());
        Permanent commander = harness.addToBattlefieldAndReturn(player1, commanderCard);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, commander.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(commander);
        assertThat(gd.playerHands.get(player1.getId())).contains(commanderCard);
        assertThat(sanctum.isTapped()).isTrue();
    }

    @Test
    void rejectsOpponentCommander() {
        Card opponentCommanderCard = new GrizzlyBears();
        opponentCommanderCard.setOwnerId(player2.getId());
        gd.makeCommander(player2.getId(), opponentCommanderCard);
        gd.playerCommandZones.get(player2.getId()).clear();
        Permanent opponentCommander = harness.addToBattlefieldAndReturn(player2, opponentCommanderCard);
        harness.addToBattlefield(player1, new SanctumOfEternity());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, opponentCommander.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
