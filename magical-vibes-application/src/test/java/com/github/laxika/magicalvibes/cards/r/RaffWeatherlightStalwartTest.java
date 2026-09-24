package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({RaffWeatherlightStalwart.class, GrizzlyBears.class, LightningBolt.class})
class RaffWeatherlightStalwartTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant may tap two creatures to draw a card")
    void castingInstantMayTapTwoCreaturesToDraw() {
        addCreatureReady(player1, new RaffWeatherlightStalwart());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        int handAfterCast = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handAfterCast + 1);
    }

    @Test
    @DisplayName("The spell-cast trigger can be declined")
    void castingInstantCanDeclineTheDraw() {
        addCreatureReady(player1, new RaffWeatherlightStalwart());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        int handAfterCast = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handAfterCast);
    }

    @Test
    @DisplayName("The activated ability boosts own creatures and grants vigilance until end of turn")
    void activatedAbilityBoostsOwnCreaturesAndGrantsVigilance() {
        Permanent raff = addCreatureReady(player1, new RaffWeatherlightStalwart());
        Permanent mine = addCreatureReady(player1, new GrizzlyBears());
        Permanent theirs = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, raff)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, raff)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, mine)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mine)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, theirs)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, theirs)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, raff, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, mine, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, theirs, Keyword.VIGILANCE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, raff)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, raff)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, mine)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mine)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, raff, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, mine, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The spell-cast trigger does not trigger for creature spells")
    void creatureSpellDoesNotTrigger() {
        addCreatureReady(player1, new RaffWeatherlightStalwart());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Raff, Weatherlight Stalwart"));
    }
}
