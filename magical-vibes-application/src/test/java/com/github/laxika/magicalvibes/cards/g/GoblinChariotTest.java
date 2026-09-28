package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SpringleafDrum;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinChariot.class, SpringleafDrum.class})
class GoblinChariotTest extends BaseCardTest {

    @Test
    @DisplayName("Haste lets Goblin Chariot attack the turn it enters")
    void hasteAllowsAttackingTheTurnItEnters() {
        harness.castFromHand(player1, new GoblinChariot(), "{2}{R}");
        harness.passBothPriorities();

        declareAttackers(List.of(0));

        Permanent chariot = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(chariot.isAttackedThisTurn()).isTrue();
        assertThat(chariot.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Haste lets Goblin Chariot be tapped for an ability the turn it enters")
    void hasteAllowsPayingATapCostTheTurnItEnters() {
        Permanent drum = harness.addToBattlefieldAndReturn(player1, new SpringleafDrum());
        harness.castFromHand(player1, new GoblinChariot(), "{2}{R}");
        harness.passBothPriorities();

        Permanent chariot = findPermanent(player1, "Goblin Chariot");
        int drumIndex = gd.playerBattlefields.get(player1.getId()).indexOf(drum);

        harness.activateAbility(player1, drumIndex, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        assertThat(drum.isTapped()).isTrue();
        assertThat(chariot.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }
}
