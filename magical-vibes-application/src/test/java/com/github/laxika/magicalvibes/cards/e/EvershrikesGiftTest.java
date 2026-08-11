package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EvershrikesGiftTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Evershrike's Gift attaches it and grants +1/+0 and flying")
    void resolvingAttachesAndBoosts() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new EvershrikesGift()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Evershrike's Gift")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bears.getId()));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Blight 2 returns Evershrike's Gift from the graveyard to its owner's hand")
    void blightReturnsGiftToHand() {
        Permanent giant = addCreatureReady(player1, new HillGiant());
        harness.setGraveyard(player1, List.of(new EvershrikesGift()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Evershrike's Gift");
        harness.assertInHand(player1, "Evershrike's Gift");
    }

    @Test
    @DisplayName("Blight 2 prompts for the creature when more than one creature is controlled")
    void blightPromptsForCreatureChoice() {
        Permanent giant = addCreatureReady(player1, new HillGiant());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new EvershrikesGift()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, giant.getId());
        harness.passBothPriorities();

        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        harness.assertInHand(player1, "Evershrike's Gift");
    }

    @Test
    @DisplayName("Blight 2 can only be activated at sorcery speed")
    void blightIsSorcerySpeed() {
        addCreatureReady(player1, new HillGiant());
        harness.setGraveyard(player1, List.of(new EvershrikesGift()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
