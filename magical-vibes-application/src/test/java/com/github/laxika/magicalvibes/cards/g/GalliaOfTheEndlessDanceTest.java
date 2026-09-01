package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SatyrsCunning;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GalliaOfTheEndlessDance.class, GrizzlyBears.class, SatyrsCunning.class})
class GalliaOfTheEndlessDanceTest extends BaseCardTest {

    @Test
    @DisplayName("Other Satyrs you control get +1/+1 and haste")
    void buffsOtherSatyrsAndNotNonSatyrs() {
        Permanent gallia = addCreatureReady(player1, new GalliaOfTheEndlessDance());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new SatyrsCunning()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent satyr = findPermanent(player1, "Satyr");
        assertThat(gqs.getEffectivePower(gd, satyr)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, satyr)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, satyr, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, gallia)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gallia)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Attacking with three creatures offers the random discard and draw")
    void attacksWithThreeCreaturesOfferDiscardAndDraw() {
        addCreatureReady(player1, new GalliaOfTheEndlessDance());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(List.of(0, 1, 2));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Attacking with fewer than three creatures does not trigger")
    void doesNotTriggerWithFewerThanThreeCreatures() {
        addCreatureReady(player1, new GalliaOfTheEndlessDance());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the attack trigger does not discard or draw")
    void decliningAttackTriggerDoesNothing() {
        addCreatureReady(player1, new GalliaOfTheEndlessDance());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Card handCard = new GrizzlyBears();
        harness.setHand(player1, List.of(handCard));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(List.of(0, 1, 2));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
