package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.z.Zombify;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FeedTheBog.class, GrizzlyBears.class, HillGiant.class, Zombify.class})
class FeedTheBogTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a low-mana-value creature and perpetually boosts creature cards in the graveyard")
    void returnsCreatureAndPerpetuallyBoostsGraveyardCreatures() {
        GrizzlyBears target = new GrizzlyBears();
        GrizzlyBears remaining = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target, remaining));
        harness.setHand(player1, List.of(new FeedTheBog()));
        addFeedTheBogMana();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        Permanent returnedTarget = findPermanentByCardId(target.getId());
        assertThat(gqs.getEffectivePower(gd, returnedTarget)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, returnedTarget)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new Zombify()));
        addZombifyMana();
        harness.castSorcery(player1, 0, remaining.getId());
        harness.passBothPriorities();

        Permanent returnedRemaining = findPermanentByCardId(remaining.getId());
        assertThat(gqs.getEffectivePower(gd, returnedRemaining)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, returnedRemaining)).isEqualTo(3);
    }

    @Test
    @DisplayName("Replicate creates an additional copy")
    void replicateCreatesAdditionalCopy() {
        GrizzlyBears target = new GrizzlyBears();
        GrizzlyBears remaining = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target, remaining));
        harness.setHand(player1, List.of(new FeedTheBog()));
        addFeedTheBogMana(1);

        harness.castInstantWithRepeatedCosts(player1, 0, target.getId(), List.of("{1}{B}"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, remaining.getId());
        resolveAllTriggers();

        Permanent returnedTarget = findPermanentByCardId(target.getId());
        Permanent returnedRemaining = findPermanentByCardId(remaining.getId());
        assertThat(gqs.getEffectivePower(gd, returnedTarget)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, returnedTarget)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, returnedRemaining)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, returnedRemaining)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a creature with mana value greater than 3")
    void cannotTargetIneligibleGraveyardCard() {
        Card largeCreature = new HillGiant();
        harness.setGraveyard(player1, List.of(largeCreature));
        harness.setHand(player1, List.of(new FeedTheBog()));
        addFeedTheBogMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, largeCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetOpponentsCreature() {
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new FeedTheBog()));
        addFeedTheBogMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetNoncreatureCard() {
        FeedTheBog target = new FeedTheBog();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new FeedTheBog()));
        addFeedTheBogMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void boostsHighManaValueCreaturesButNotOpponentsGraveyard() {
        GrizzlyBears target = new GrizzlyBears();
        HillGiant largeCreature = new HillGiant();
        GrizzlyBears opponentsCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target, largeCreature));
        harness.setGraveyard(player2, List.of(opponentsCreature));
        harness.setHand(player1, List.of(new FeedTheBog()));
        addFeedTheBogMana();
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Zombify()));
        addZombifyMana();
        harness.castSorcery(player1, 0, largeCreature.getId());
        harness.passBothPriorities();
        Permanent returnedLargeCreature = findPermanent(player1, "Hill Giant");
        assertThat(gqs.getEffectivePower(gd, returnedLargeCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, returnedLargeCreature)).isEqualTo(4);

        harness.setHand(player2, List.of(new Zombify()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, opponentsCreature.getId());
        harness.passBothPriorities();
        Permanent returnedOpponent = findPermanent(player2, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, returnedOpponent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, returnedOpponent)).isEqualTo(2);
    }

    @Test
    void unchangedReplicateTargetMakesOriginalFailWithoutAnotherBoost() {
        GrizzlyBears target = new GrizzlyBears();
        GrizzlyBears remaining = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target, remaining));
        harness.setHand(player1, List.of(new FeedTheBog()));
        addFeedTheBogMana(1);
        harness.castInstantWithRepeatedCosts(player1, 0, target.getId(), List.of("{1}{B}"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        Permanent returnedTarget = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, returnedTarget)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, returnedTarget)).isEqualTo(3);
        harness.setHand(player1, List.of(new Zombify()));
        addZombifyMana();
        harness.castSorcery(player1, 0, remaining.getId());
        harness.passBothPriorities();
        Permanent returnedRemaining = findPermanentByCardId(remaining.getId());
        assertThat(gqs.getEffectivePower(gd, returnedRemaining)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, returnedRemaining)).isEqualTo(3);
    }

    private void addFeedTheBogMana() {
        addFeedTheBogMana(0);
    }

    private void addFeedTheBogMana(int replicatePayments) {
        harness.addMana(player1, ManaColor.BLACK, 1 + replicatePayments);
        harness.addMana(player1, ManaColor.COLORLESS, 1 + replicatePayments);
    }

    private void addZombifyMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private Permanent findPermanentByCardId(java.util.UUID cardId) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElseThrow();
    }
}
