package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KazuulTyrantOfTheCliffsTest extends BaseCardTest {

    @Test
    @DisplayName("Declining to pay creates a 3/3 red Ogre token")
    void decliningToPayCreatesOgreToken() {
        addKazuul(player1);
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(findPermanents(player1, "Ogre")).singleElement().satisfies(ogre -> {
            assertThat(ogre.getCard().isToken()).isTrue();
            assertThat(ogre.getCard().getPower()).isEqualTo(3);
            assertThat(ogre.getCard().getToughness()).isEqualTo(3);
            assertThat(ogre.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(ogre.getCard().getSubtypes()).contains(CardSubtype.OGRE);
        });
    }

    @Test
    @DisplayName("The attacking creature's controller may pay to prevent the token")
    void attackerControllerMayPay() {
        addKazuul(player1);
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(findPermanents(player1, "Ogre")).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Each attacking creature causes a separate token-or-payment trigger")
    void triggersForEachAttackingCreature() {
        addKazuul(player1);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0, 1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(findPermanents(player1, "Ogre")).hasSize(2);
    }

    private void addKazuul(Player player) {
        addCreatureReady(player, new KazuulTyrantOfTheCliffs());
    }
}
