package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CandyGrapple;
import com.github.laxika.magicalvibes.cards.e.EdgewallPack;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FaunsbaneTroll.class, EdgewallPack.class, CandyGrapple.class})
class FaunsbaneTrollTest extends BaseCardTest {

    @Test
    void entersWithMonsterRoleAttachedAndGetsItsBonus() {
        Permanent troll = castTroll();

        Permanent role = findPermanent(player1, "Monster");
        assertThat(role.getCard().isToken()).isTrue();
        assertThat(role.getCard().isAura()).isTrue();
        assertThat(role.getCard().getSubtypes()).contains(CardSubtype.ROLE);
        assertThat(role.getAttachedTo()).isEqualTo(troll.getId());
        assertThat(gqs.getEffectivePower(gd, troll)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, troll)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, troll, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void sacrificesAttachedRoleFightsAndExilesCreatureThatWouldDie() {
        Permanent troll = castTroll();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EdgewallPack());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(troll),
                0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Monster");
        harness.assertNotOnBattlefield(player2, "Edgewall Pack");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Edgewall Pack"));
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(troll);
    }

    @Test
    void cannotActivateWithoutAnAuraAttachedToThisCreature() {
        Permanent troll = addReadyTroll();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EdgewallPack());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(troll),
                0,
                null,
                target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void createsRoleUnderTriggerControllersControlAfterTrollChangesController() {
        Permanent troll = harness.enterBattlefieldAndReturn(player1, new FaunsbaneTroll());
        gd.playerBattlefields.get(player1.getId()).remove(troll);
        gd.playerBattlefields.get(player2.getId()).add(troll);

        harness.passBothPriorities();

        Permanent role = findPermanent(player1, "Monster");
        assertThat(role.getAttachedTo()).isEqualTo(troll.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(troll);
        assertThat(gqs.getEffectivePower(gd, troll)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, troll, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void doesNotCreateRoleIfTrollLeavesBeforeEntryTriggerResolves() {
        Permanent troll = harness.enterBattlefieldAndReturn(player1, new FaunsbaneTroll());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, troll));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Monster");
        harness.assertInGraveyard(player1, "Faunsbane Troll");
    }

    @Test
    void cannotSacrificeRoleAttachedToAnotherCreature() {
        Permanent otherTroll = castTroll();
        Permanent troll = addReadyTroll();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EdgewallPack());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(troll), 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Monster").getAttachedTo()).isEqualTo(otherTroll.getId());
    }

    @Test
    void cannotSacrificeOpponentsAuraAttachedToTroll() {
        Permanent troll = castTroll();
        Permanent role = findPermanent(player1, "Monster");
        gd.playerBattlefields.get(player1.getId()).remove(role);
        gd.playerBattlefields.get(player2.getId()).add(role);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EdgewallPack());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(troll), 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(role);
    }

    @Test
    void cannotTargetCreatureYouControl() {
        Permanent troll = castTroll();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EdgewallPack());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(troll), 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Monster");
    }

    @Test
    void cannotActivateDuringCombat() {
        Permanent troll = castTroll();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EdgewallPack());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(troll), 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Monster");
    }

    @Test
    void fightKillsBothCreaturesButExilesOnlyTheTarget() {
        Permanent troll = castTroll();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FaunsbaneTroll());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(troll),
                0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Faunsbane Troll");
        harness.assertNotInGraveyard(player2, "Faunsbane Troll");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
        harness.assertNotOnBattlefield(player1, "Faunsbane Troll");
        harness.assertNotOnBattlefield(player2, "Faunsbane Troll");
    }

    @Test
    void stillExilesTargetDyingLaterWhenTrollLeftBeforeFightResolved() {
        Permanent troll = castTroll();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EdgewallPack());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(troll),
                0, null, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, troll));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Edgewall Pack");

        harness.setHand(player1, List.of(new CandyGrapple()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Edgewall Pack");
        harness.assertNotInGraveyard(player2, "Edgewall Pack");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Edgewall Pack"));
    }

    @Test
    void sacrificesRoleImmediatelyAndFightsUsingUnenchantedPower() {
        Permanent troll = castTroll();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FaunsbaneTroll());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(troll),
                0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Monster");
        assertThat(gqs.getEffectivePower(gd, troll)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, troll, Keyword.TRAMPLE)).isFalse();
        harness.assertOnBattlefield(player2, "Faunsbane Troll");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Faunsbane Troll");
        harness.assertOnBattlefield(player2, "Faunsbane Troll");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void exileReplacementExpiresAtEndOfTurn() {
        Permanent troll = castTroll();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EdgewallPack());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(troll),
                0, null, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, troll));
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player2, List.of(new CandyGrapple()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        harness.assertInGraveyard(player2, "Edgewall Pack");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    private Permanent castTroll() {
        harness.castFromHand(player1, new FaunsbaneTroll(), "{2}{B}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Faunsbane Troll");
    }

    private Permanent addReadyTroll() {
        return addCreatureReady(player1, new FaunsbaneTroll());
    }
}
