package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AjaniVengeant;
import com.github.laxika.magicalvibes.cards.c.CallToHeel;
import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.e.ElvishVisionary;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VithianStinger.class, CylianElf.class, ElvishVisionary.class, AjaniVengeant.class, CallToHeel.class})
@DisplayName("Vithian Stinger")
class VithianStingerTest extends BaseCardTest {

    @Test
    @DisplayName("{T} deals 1 damage to target player")
    void deals1DamageToPlayer() {
        harness.setLife(player2, 20);
        addReadyStinger(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("{T} deals 1 damage to target creature, destroying a 1/1")
    void deals1DamageDestroying1Toughness() {
        addReadyStinger(player1);
        harness.addToBattlefield(player2, new ElvishVisionary());

        UUID targetId = harness.getPermanentId(player2, "Elvish Visionary");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Elvish Visionary");
    }

    @Test
    @DisplayName("{T} does not kill a 2/2 creature")
    void deals1DamageDoesNotKill2Toughness() {
        addReadyStinger(player1);
        harness.addToBattlefield(player2, new CylianElf());

        UUID targetId = harness.getPermanentId(player2, "Cylian Elf");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Cylian Elf");
    }

    @Test
    @DisplayName("Ability taps the source")
    void abilityTapsSource() {
        Permanent stinger = addReadyStinger(player1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(stinger.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent stinger = addReadyStinger(player1);
        stinger.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Unearth returns Vithian Stinger to the battlefield with haste")
    void unearthReturnsWithHaste() {
        VithianStinger card = new VithianStinger();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Vithian Stinger");
        assertThat(perm.getGrantedKeywords()).contains(Keyword.HASTE);
        harness.assertNotInGraveyard(player1, "Vithian Stinger");
    }

    @Test
    @DisplayName("Unearthed Vithian Stinger is exiled at the next end step")
    void unearthExiledAtEndStep() {
        VithianStinger card = new VithianStinger();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.passUntil(TurnStep.END_STEP);

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Vithian Stinger");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Vithian Stinger"));
    }

    @Test
    void unearthHasteAllowsImmediateTapAndLethalDamageExilesIt() {
        VithianStinger card = new VithianStinger();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        UUID stingerId = harness.getPermanentId(player1, "Vithian Stinger");
        harness.activateAbility(player1, 0, null, stingerId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vithian Stinger");
        harness.assertNotInGraveyard(player1, "Vithian Stinger");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    @Test
    void tapAbilityRequiresNoSummoningSickness() {
        harness.addToBattlefield(player1, new VithianStinger());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void unearthRequiresMainPhase() {
        harness.setGraveyard(player1, List.of(new VithianStinger()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Vithian Stinger");
        harness.assertNotOnBattlefield(player1, "Vithian Stinger");
    }

    @Test
    void unearthRequiresOwnTurn() {
        harness.setGraveyard(player2, List.of(new VithianStinger()));
        harness.addMana(player2, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player2, "Vithian Stinger");
    }

    @Test
    void unearthRequiresEmptyStack() {
        addReadyStinger(player1);
        harness.setGraveyard(player1, List.of(new VithianStinger()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, null, player2.getId());

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Vithian Stinger");
    }

    @Test
    void unearthRequiresRedMana() {
        harness.setGraveyard(player1, List.of(new VithianStinger()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Vithian Stinger");
        harness.assertNotOnBattlefield(player1, "Vithian Stinger");
    }

    @Test
    void dealsDamageToPlaneswalker() {
        addReadyStinger(player1);
        Permanent ajani = harness.addToBattlefieldAndReturn(player2, new AjaniVengeant());
        ajani.setCounterCount(CounterType.LOYALTY, 3);

        harness.activateAbility(player1, 0, null, ajani.getId());
        harness.passBothPriorities();

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Ajani Vengeant");
    }

    @Test
    void returningUnearthedStingerToHandExilesIt() {
        VithianStinger card = new VithianStinger();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new CallToHeel()));
        harness.setLibrary(player1, List.of(new CylianElf()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Vithian Stinger"));

        harness.assertNotOnBattlefield(player1, "Vithian Stinger");
        harness.assertNotInHand(player1, "Vithian Stinger");
        harness.assertNotInGraveyard(player1, "Vithian Stinger");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        harness.assertInHand(player1, "Cylian Elf");
    }

    @Test
    void abilityStillResolvesAfterSourceLeavesBattlefield() {
        addReadyStinger(player1);
        harness.setHand(player1, List.of(new CallToHeel()));
        harness.setLibrary(player1, List.of(new CylianElf()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        UUID stingerId = harness.getPermanentId(player1, "Vithian Stinger");
        harness.activateAbility(player1, 0, null, player2.getId());

        harness.castAndResolveInstant(player1, 0, stingerId);
        harness.assertInHand(player1, "Vithian Stinger");
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    private Permanent addReadyStinger(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new VithianStinger());
        perm.setSummoningSick(false);
        return perm;
    }
}
