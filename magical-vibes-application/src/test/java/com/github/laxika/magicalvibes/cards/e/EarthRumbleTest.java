package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.PhantasmalBear;
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

@CardUsed({EarthRumble.class, Forest.class, HillGiant.class, GrizzlyBears.class})
class EarthRumbleTest extends BaseCardTest {

    @Test
    void earthbendsThenFightsChosenCreatures() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EarthRumble()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0, land.getId());

        PendingInteraction.PermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(firstChoice.validPermanentIds()).contains(ownCreature.getId());
        assertThat(firstChoice.validPlayerIds()).contains(player1.getId());

        harness.handlePermanentChosen(player1, ownCreature.getId());
        PendingInteraction.PermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(secondChoice.validPermanentIds()).containsExactly(opposingCreature.getId());

        harness.handlePermanentChosen(player1, opposingCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void mayChooseNoCreatureToFight() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EarthRumble()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0, land.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, opposingCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, land)).isTrue();
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(ownCreature.getMarkedDamage()).isZero();
    }

    @Test
    void earthbentLandCanFightAndReturnsTappedAfterDying() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EarthRumble()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0, land.getId());
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(land.getId());
        harness.handlePermanentChosen(player1, land.getId());
        harness.handlePermanentChosen(player1, opposingCreature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
        Permanent returnedLand = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(returnedLand.getId()).isNotEqualTo(land.getId());
        assertThat(returnedLand.isTapped()).isTrue();
        assertThat(returnedLand.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.isCreature(gd, returnedLand)).isFalse();
    }

    @Test
    void earthbendsEvenWithoutAnOpposingCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new EarthRumble()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0, land.getId());

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Earth Rumble");
    }

    @Test
    @CardUsed({PhantasmalBear.class})
    void becomingTargetOfFightTriggersBeforeFightResolves() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new PhantasmalBear());
        harness.setHand(player1, List.of(new EarthRumble()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0, land.getId());
        harness.handlePermanentChosen(player1, land.getId());
        harness.handlePermanentChosen(player1, opposingCreature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Phantasmal Bear");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(land.getMarkedDamage()).isZero();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(land.getMarkedDamage()).isZero();
    }

    @Test
    @CardUsed({PhantasmalBear.class})
    void decliningFriendlyTargetStillTargetsOpposingCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new PhantasmalBear());
        harness.setHand(player1, List.of(new EarthRumble()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0, land.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, opposingCreature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Phantasmal Bear");
        assertThat(land.getMarkedDamage()).isZero();
    }
}
