package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EdricSpymasterOfTrest.class, GrizzlyBears.class, Forest.class})
class EdricSpymasterOfTrestTest extends BaseCardTest {

    @Test
    @DisplayName("A creature dealing combat damage to an opponent lets its controller draw")
    void creaturesControllerMayDrawWhenDamagingAnOpponent() {
        harness.addToBattlefield(player1, new EdricSpymasterOfTrest());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        addAttackingCreature(player1, new GrizzlyBears());
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The attacking creature's controller may decline the draw")
    void creaturesControllerMayDeclineTheDraw() {
        harness.addToBattlefield(player1, new EdricSpymasterOfTrest());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        addAttackingCreature(player1, new GrizzlyBears());
        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    @DisplayName("A creature dealing combat damage to you does not trigger")
    void creatureDamagingYouDoesNotTrigger() {
        harness.addToBattlefield(player1, new EdricSpymasterOfTrest());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest()));

        addAttackingCreature(player2, new GrizzlyBears());
        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    private Permanent addAttackingCreature(Player player, Card card) {
        Permanent permanent = addCreatureReady(player, card);
        permanent.setAttacking(true);
        return permanent;
    }
}
