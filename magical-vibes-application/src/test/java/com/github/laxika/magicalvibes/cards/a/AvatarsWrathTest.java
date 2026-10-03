package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.h.HogMonkey;
import com.github.laxika.magicalvibes.cards.m.MishrasFactory;
import com.github.laxika.magicalvibes.cards.f.FireNationAttacks;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvatarsWrath.class, HogMonkey.class, AvatarAang.class, AirbendersReversal.class,
        MishrasFactory.class, FireNationAttacks.class, Plains.class})
class AvatarsWrathTest extends BaseCardTest {

    @Test
    void airbendsAllOtherCreaturesAndExilesItself() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new HogMonkey());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new HogMonkey());
        AvatarsWrath wrath = new AvatarsWrath();
        harness.setHand(player1, List.of(wrath));
        addAvatarWrathMana();

        harness.castAndResolveSorcery(player1, 0, chosen.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(chosen);
        assertThat(gd.findExiledCard(other.getOriginalCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(wrath.getId())).isNotNull();
    }

    @Test
    void withoutATargetAirbendsEveryCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new HogMonkey());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HogMonkey());
        AvatarsWrath wrath = new AvatarsWrath();
        harness.setHand(player1, List.of(wrath));
        addAvatarWrathMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.findExiledCard(first.getOriginalCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(second.getOriginalCard().getId())).isNotNull();
    }

    @Test
    void opponentsCannotCastAirbentCreaturesUntilYourNextTurn() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new HogMonkey());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new HogMonkey());
        harness.setHand(player1, List.of(new AvatarsWrath()));
        addAvatarWrathMana();

        harness.castAndResolveSorcery(player1, 0, chosen.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromExile(player2, other.getOriginalCard().getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be cast");
    }

    @Test
    void doesNotResolveWhenItsChosenTargetLeavesTheBattlefield() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new HogMonkey());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new HogMonkey());
        AvatarsWrath wrath = new AvatarsWrath();
        harness.setHand(player1, List.of(wrath));
        harness.setHand(player2, List.of(new AirbendersReversal()));
        addAvatarWrathMana();
        harness.castSorcery(player1, 0, chosen.getId());

        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castModalInstant(player2, 0, 1, List.of(chosen.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(other);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(wrath);
        assertThat(gd.findExiledCard(wrath.getId())).isNull();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castFromExile(player2, chosen.getOriginalCard().getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Hog-Monkey");
    }

    @Test
    void doesNotTriggerAirbendingWhenTheOnlyCreatureIsChosen() {
        Permanent aang = harness.addToBattlefieldAndReturn(player1, new AvatarAang());
        harness.setHand(player1, List.of(new AvatarsWrath()));
        addAvatarWrathMana();

        harness.castAndResolveSorcery(player1, 0, aang.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aang);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void controllerCanRecastAnAirbentCreatureForTwoGenericMana() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HogMonkey());
        harness.setHand(player1, List.of(new AvatarsWrath()));
        addAvatarWrathMana();
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, creature.getOriginalCard().getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hog-Monkey");
        assertThat(gd.findExiledCard(creature.getOriginalCard().getId())).isNull();
    }

    @Test
    void restrictionLastsThroughOpponentsTurnAndExpiresOnControllersNextTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HogMonkey());
        harness.setHand(player1, List.of(new AvatarsWrath()));
        addAvatarWrathMana();
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.castFromExile(player2, creature.getOriginalCard().getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be cast");

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castFromExile(player2, creature.getOriginalCard().getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hog-Monkey");
    }

    @Test
    void opponentsCanStillCastCreaturesFromTheirHands() {
        harness.castFromHand(player1, new AvatarsWrath(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player2, new HogMonkey(), "{2}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hog-Monkey");
    }

    @Test
    void airbendingAnAnimatedLandDoesNotGrantPermissionToPlayIt() {
        Permanent factory = harness.addToBattlefieldAndReturn(player1, new MishrasFactory());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new AvatarsWrath()));
        addAvatarWrathMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.findExiledCard(factory.getOriginalCard().getId())).isNotNull();
        assertThatThrownBy(() -> harness.castFromExile(player1, factory.getOriginalCard().getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void leavesNoncreaturePermanentsOnTheBattlefield() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HogMonkey());
        harness.setHand(player1, List.of(new AvatarsWrath()));
        addAvatarWrathMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(gd.findExiledCard(creature.getOriginalCard().getId())).isNotNull();
    }

    @Test
    void opponentsCannotCastFlashbackSpells() {
        FireNationAttacks attacks = new FireNationAttacks();
        harness.setGraveyard(player2, List.of(attacks));
        harness.castFromHand(player1, new AvatarsWrath(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.castFlashback(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be cast");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(attacks);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void airbentCreatureTokensCeaseToExist() {
        harness.castFromHand(player1, new FireNationAttacks(), "{4}{R}");
        harness.passBothPriorities();
        List<java.util.UUID> tokenIds = gd.playerBattlefields.get(player1.getId()).stream()
                .map(permanent -> permanent.getOriginalCard().getId()).toList();
        assertThat(tokenIds).hasSize(2);
        harness.castFromHand(player1, new AvatarsWrath(), "{2}{W}{W}");

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        for (java.util.UUID tokenId : tokenIds) {
            assertThat(gd.findExiledCard(tokenId)).isNull();
        }
    }

    private void addAvatarWrathMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
