package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.p.ProfaneTutor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheFaceOfBoe.class, ProfaneTutor.class})
class TheFaceOfBoeTest extends BaseCardTest {

    @Test
    @DisplayName("Offers a suspended spell and casts it for its suspend cost")
    void castsSpellForSuspendCost() {
        TheFaceOfBoe faceOfBoe = new TheFaceOfBoe();
        ProfaneTutor tutor = new ProfaneTutor();
        harness.addToBattlefield(player1, faceOfBoe);
        gd.playerBattlefields.get(player1.getId()).getFirst().setSummoningSick(false);
        harness.setHand(player1, List.of(tutor));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(tutor);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == tutor);
    }
}
