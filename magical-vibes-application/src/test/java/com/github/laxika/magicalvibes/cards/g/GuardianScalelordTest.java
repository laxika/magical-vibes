package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.ThunderingGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({GuardianScalelord.class, Forest.class, GiantGrowth.class, GrizzlyBears.class, ThunderingGiant.class})
class GuardianScalelordTest extends BaseCardTest {

    @Test
    @DisplayName("Backup gives another creature a counter, flying, and the attack trigger")
    void backupGrantsAnotherCreatureAbilities() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Card eligible = new GrizzlyBears();
        Card land = new Forest();
        Card nonPermanent = new GiantGrowth();
        Card tooExpensive = new ThunderingGiant();
        harness.setGraveyard(player1, List.of(eligible, land, nonPermanent, tooExpensive));

        castGuardianTargeting(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(eligible.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(eligible.getId()));
    }

    @Test
    @DisplayName("Backup on Guardian Scalelord only puts a counter on itself")
    void backupTargetingSelfDoesNotGrantAnotherCreatureAbility() {
        harness.castFromHand(player1, new GuardianScalelord(), "{4}{W}");
        harness.passBothPriorities();
        Permanent guardian = findPermanent(player1, "Guardian Scalelord");
        harness.handlePermanentChosen(player1, guardian.getId());
        harness.passBothPriorities();

        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    void guardianUsesBoostedPowerForItsOwnAttackTrigger() {
        Permanent guardian = addCreatureReady(player1, new GuardianScalelord());
        Card eligible = new ThunderingGiant();
        harness.setGraveyard(player1, List.of(eligible));
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, guardian.getId());

        declareAttackers(player1, List.of(0));
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thundering Giant");
        harness.assertNotInGraveyard(player1, "Thundering Giant");
    }

    @Test
    void backupUsesRecipientsPowerRatherThanGuardiansPower() {
        Permanent target = addCreatureReady(player1, new ThunderingGiant());
        Card eligible = new GuardianScalelord();
        harness.setGraveyard(player1, List.of(eligible));
        castGuardianTargeting(target);

        declareAttackers(player1, List.of(0));
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Guardian Scalelord")).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Guardian Scalelord");
    }

    @Test
    void backupAbilitiesExpireButCounterRemains() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        castGuardianTargeting(target);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
        declareAttackers(player1, List.of(0));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void backupCanTargetOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castGuardianTargeting(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    private void castGuardianTargeting(Permanent target) {
        harness.castFromHand(player1, new GuardianScalelord(), "{4}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
