package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpecterOfMortality.class, Forest.class, GiantSpider.class, HillGiant.class})
class SpecterOfMortalityTest extends BaseCardTest {

    @Test
    @DisplayName("Exiled creature cards determine the -X/-X applied to every other creature")
    void exiledCreatureCountDeterminesOtherCreatureDebuff() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        GiantSpider graveyardSpider = new GiantSpider();
        HillGiant graveyardGiant = new HillGiant();
        harness.setGraveyard(player1, List.of(graveyardSpider, graveyardGiant));

        castSpecter();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(graveyardSpider.getId(), graveyardGiant.getId()));
        harness.passBothPriorities();

        Permanent specter = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SpecterOfMortality)
                .findFirst()
                .orElseThrow();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getId())
                .containsExactlyInAnyOrder(graveyardSpider.getId(), graveyardGiant.getId());
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, specter)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, specter)).isEqualTo(3);
    }

    @Test
    @DisplayName("The controller may exile no creature cards and then nothing is weakened")
    void exilingNoCardsDoesNotWeakenCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        GiantSpider graveyardSpider = new GiantSpider();
        harness.setGraveyard(player1, List.of(graveyardSpider));

        castSpecter();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Only creature cards are offered and the debuff wears off at end of turn")
    void onlyCreatureCardsAreOfferedAndDebuffExpires() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        GiantSpider graveyardSpider = new GiantSpider();
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(graveyardSpider, forest));

        castSpecter();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(graveyardSpider.getId());
        harness.handleMultipleCardsChosen(player1, List.of(graveyardSpider.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(forest);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    private void castSpecter() {
        harness.setHand(player1, List.of(new SpecterOfMortality()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
