package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DenethorStoneSeer.class, GrizzlyBears.class})
class DenethorStoneSeerTest extends BaseCardTest {

    @Test
    void scriesTwoOnEnter() {
        List<Card> library = gd.playerDecks.get(player1.getId());
        library.add(0, new GrizzlyBears());
        library.add(1, new GrizzlyBears());

        harness.setHand(player1, List.of(new DenethorStoneSeer()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(2);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
    }

    @Test
    void targetPlayerBecomesMonarchAndAnyTargetTakesDamage() {
        Permanent denethor = addCreatureReady(player1, new DenethorStoneSeer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(player2.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());
        harness.assertInGraveyard(player1, "Denethor, Stone Seer");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(denethor.isTapped()).isTrue();
    }

    @Test
    void requiresPlayerThenAnyTarget() {
        Permanent denethor = addCreatureReady(player1, new DenethorStoneSeer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(target.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(denethor.isTapped()).isFalse();
    }
}
