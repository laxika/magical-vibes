package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.CaptainMarvelEarthsProtector;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.j.JenniferWalters;
import com.github.laxika.magicalvibes.cards.s.SHIELDHelicarrier;
import com.github.laxika.magicalvibes.cards.s.SHIELDSpyKit;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NickFuryAgentOfSHIELD.class, CaptainMarvelEarthsProtector.class, Shock.class,
        Island.class, JenniferWalters.class, SHIELDHelicarrier.class, SHIELDSpyKit.class})
class NickFuryAgentOfSHIELDTest extends BaseCardTest {

    @Test
    void powerUpPutsTwoCountersAndAHeroOntoTheBattlefield() {
        Permanent nickFury = harness.enterBattlefieldAndReturn(player1, new NickFuryAgentOfSHIELD());
        Card hero = new CaptainMarvelEarthsProtector();
        setLibrary(hero, new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock());
        addDiscountedPowerUpMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(hero.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(hero.getId()));

        assertThat(nickFury.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard().getId().equals(hero.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void powerUpMayPutNothingOntoTheBattlefield() {
        Permanent nickFury = harness.enterBattlefieldAndReturn(player1, new NickFuryAgentOfSHIELD());
        setLibrary(new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock());
        addDiscountedPowerUpMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(nickFury.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(7);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void powerUpCanBeActivatedOnlyOnce() {
        harness.enterBattlefieldAndReturn(player1, new NickFuryAgentOfSHIELD());
        addDiscountedPowerUpMana();
        addDiscountedPowerUpMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    void mayDeclineAnEligibleHeroAndLeaveUnseenCardsOnTop() {
        Permanent nickFury = harness.enterBattlefieldAndReturn(player1, new NickFuryAgentOfSHIELD());
        Card hero = new CaptainMarvelEarthsProtector();
        Card unseen = new Island();
        setLibrary(hero, new Island(), new Island(), new Island(), new Island(), new Island(), new Island(), unseen);
        addDiscountedPowerUpMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(nickFury.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(nickFury);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(8).first().isSameAs(unseen);
        assertThat(gd.playerDecks.get(player1.getId())).contains(hero);
    }

    @Test
    void canSelectEquipmentFromAShortLibrary() {
        harness.enterBattlefieldAndReturn(player1, new NickFuryAgentOfSHIELD());
        Card equipment = new SHIELDSpyKit();
        Card land = new Island();
        setLibrary(equipment, land);
        addDiscountedPowerUpMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(equipment.getId()));

        harness.assertOnBattlefield(player1, "S.H.I.E.L.D. Spy Kit");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canSelectAVehicleAndItsEnterAbilityStillTriggers() {
        harness.enterBattlefieldAndReturn(player1, new NickFuryAgentOfSHIELD());
        Card vehicle = new SHIELDHelicarrier();
        setLibrary(vehicle);
        addDiscountedPowerUpMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(vehicle.getId()));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "S.H.I.E.L.D. Helicarrier");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void transformChoiceHappensDuringPowerUpResolution() {
        harness.enterBattlefieldAndReturn(player1, new NickFuryAgentOfSHIELD());
        Card hero = new JenniferWalters();
        setLibrary(hero, new Island());
        addDiscountedPowerUpMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(hero.getId()));

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.pendingEffectResolutionEntry).isNotNull();
        assertThat(gd.stack).isEmpty();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "The Sensational She-Hulk");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayKeepADoubleFacedHeroOnItsFrontFace() {
        harness.enterBattlefieldAndReturn(player1, new NickFuryAgentOfSHIELD());
        Card hero = new JenniferWalters();
        setLibrary(hero);
        addDiscountedPowerUpMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(hero.getId()));

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Jennifer Walters");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getOriginalCard().getId().equals(hero.getId()))
                .allMatch(permanent -> !permanent.isTransformed());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void requiresFullCostWhenNickFuryDidNotEnterThisTurn() {
        harness.addToBattlefield(player1, new NickFuryAgentOfSHIELD());
        setLibrary();
        addDiscountedPowerUpMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst()
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void stillRecruitsAHeroIfNickFuryDiesInResponse() {
        Permanent nickFury = harness.enterBattlefieldAndReturn(player1, new NickFuryAgentOfSHIELD());
        Card hero = new CaptainMarvelEarthsProtector();
        setLibrary(hero);
        addDiscountedPowerUpMana();
        Card shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.castAndResolveInstant(player2, 0, nickFury.getId());
        harness.assertInGraveyard(player1, "Nick Fury, Agent of S.H.I.E.L.D.");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(hero.getId()));

        harness.assertOnBattlefield(player1, "Captain Marvel, Earth's Protector");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addDiscountedPowerUpMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
