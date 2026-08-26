package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Deification.class, JaceBeleren.class, GarrukWildspeaker.class,
        GrizzlyBears.class, LightningBolt.class})
class DeificationTest extends BaseCardTest {

    @Test
    void choosesAPlaneswalkerType() {
        harness.setHand(player1, List.of(new Deification()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.prompt()).isEqualTo("Choose a planeswalker type.");
        assertThat(choice.options()).contains("JACE", "GARRUK").doesNotContain("GOBLIN");

        harness.handleListChoice(player1, "JACE");

        assertThat(findPermanent(player1, "Deification").getChosenSubtype()).isEqualTo(CardSubtype.JACE);
    }

    @Test
    void grantsHexproofOnlyToOwnPlaneswalkersOfChosenType() {
        Permanent deification = harness.addToBattlefieldAndReturn(player1, new Deification());
        deification.setChosenSubtype(CardSubtype.JACE);
        Permanent ownJace = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        Permanent ownGarruk = harness.addToBattlefieldAndReturn(player1, new GarrukWildspeaker());
        Permanent opponentJace = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());

        assertThat(gqs.hasKeyword(gd, ownJace, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownGarruk, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentJace, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void preservesOneLoyaltyWhenControllingACreature() {
        Permanent deification = harness.addToBattlefieldAndReturn(player1, new Deification());
        deification.setChosenSubtype(CardSubtype.JACE);
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 3);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, jace.getId());
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.damageDealtToPermanentsThisTurn.get(jace.getId())).isEqualTo(3);
    }

    @Test
    void doesNotPreserveLoyaltyWithoutAControlledCreature() {
        Permanent deification = harness.addToBattlefieldAndReturn(player1, new Deification());
        deification.setChosenSubtype(CardSubtype.JACE);
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 3);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, jace.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Jace Beleren");
    }
}
