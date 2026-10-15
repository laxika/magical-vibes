package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Fireball;
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

@CardUsed({RaffWeatherlightStalwart.class, GrizzlyBears.class, LightningBolt.class, Fireball.class})
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

    @Test
    @DisplayName("A sorcery triggers the draw before the spell resolves")
    void sorceryTriggersBeforeResolving() {
        Permanent raff = addCreatureReady(player1, new RaffWeatherlightStalwart());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Fireball()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 1, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.assertLife(player2, 20);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(raff.isTapped()).isTrue();
        assertThat(bear.isTapped()).isTrue();
        harness.assertInHand(player1, "Grizzly Bears");
        resolveAllTriggers();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Raff and another summoning-sick creature can pay the draw cost")
    void summoningSickCreaturesCanPayTapCost() {
        Permanent raff = harness.addToBattlefieldAndReturn(player1, new RaffWeatherlightStalwart());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        raff.setSummoningSick(true);
        bear.setSummoningSick(true);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(raff.isTapped()).isTrue();
        assertThat(bear.isTapped()).isTrue();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("One untapped creature cannot pay the cost even with other tapped creatures")
    void insufficientUntappedCreaturesDoNotDraw() {
        Permanent raff = addCreatureReady(player1, new RaffWeatherlightStalwart());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.tap();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }
        resolveAllTriggers();

        assertThat(raff.isTapped()).isFalse();
        assertThat(bear.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("An opponent casting an instant does not trigger Raff")
    void opponentInstantDoesNotTrigger() {
        addCreatureReady(player1, new RaffWeatherlightStalwart());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Raff, Weatherlight Stalwart"));
        resolveAllTriggers();
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("The activated ability works while Raff is tapped and only affects creatures present at resolution")
    void activatedAbilityDoesNotAffectLaterCreatures() {
        Permanent raff = addCreatureReady(player1, new RaffWeatherlightStalwart());
        raff.tap();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent lateCreature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, raff)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, raff, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, lateCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lateCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lateCreature, Keyword.VIGILANCE)).isFalse();
    }
}
