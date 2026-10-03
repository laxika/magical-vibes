package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LordOfExtinction;
import com.github.laxika.magicalvibes.cards.p.Panharmonicon;
import com.github.laxika.magicalvibes.cards.r.ReaverTitan;
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

@CardUsed({DrachNyen.class, GrizzlyBears.class, LordOfExtinction.class, Panharmonicon.class,
        ReaverTitan.class})
class DrachNyenTest extends BaseCardTest {

    @Test
    void exilesUpToOneCreatureAndBoostsEquippedCreatureByItsPower() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent host = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castDrachNyen(target.getId());
        Permanent drachNyen = findPermanent(player1, "Drach'Nyen");

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getOriginalCard());
        assertThat(gd.getImprintedCard(drachNyen.getCard())).isSameAs(target.getOriginalCard());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(drachNyen),
                null, host.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, host, Keyword.MENACE)).isTrue();
    }

    @Test
    void mayDeclineTheCreatureExile() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new DrachNyen(), "{4}{B}{R}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        Permanent drachNyen = findPermanent(player1, "Drach'Nyen");
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(drachNyen),
                null, host.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, host, Keyword.MENACE)).isTrue();
    }

    private void castDrachNyen(java.util.UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DrachNyen()));
        addDrachNyenMana();
        harness.castArtifact(player1, 0, targetId);
        resolveAllTriggers();
    }

    @Test
    void usesPowerInExileWithoutBattlefieldCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent host = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castDrachNyen(target.getId());
        equipDrachNyen(host);

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(2);
    }

    @Test
    void evaluatesCharacteristicDefiningPowerInExileAndUpdatesIt() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LordOfExtinction());
        Permanent host = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castDrachNyen(target.getId());
        equipDrachNyen(host);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getOriginalCard());
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(5);
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(2);
    }

    @Test
    void sumsPowerOfCardsExiledByBothEnterTriggers() {
        harness.addToBattlefield(player1, new Panharmonicon());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent host = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new DrachNyen(), "{4}{B}{R}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        resolveAllTriggers();
        equipDrachNyen(host);

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .contains(first.getOriginalCard(), second.getOriginalCard());
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, host, Keyword.MENACE)).isTrue();
    }

    @Test
    void losesPowerBonusWhenTheLinkedCardLeavesExileButKeepsMenace() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent host = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castDrachNyen(target.getId());
        equipDrachNyen(host);
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(4);

        gd.removeFromExile(target.getOriginalCard().getId());
        gd.addCardToHand(player2.getId(), target.getOriginalCard());

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, host, Keyword.MENACE)).isTrue();
    }

    private void equipDrachNyen(Permanent host) {
        Permanent drachNyen = findPermanent(player1, "Drach'Nyen");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(drachNyen),
                null, host.getId());
        harness.passBothPriorities();
    }

    @Test
    void usesPrintedPowerOfAnExiledCrewedVehicle() {
        Permanent crew = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        crew.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent titan = harness.addToBattlefieldAndReturn(player2, new ReaverTitan());
        Permanent host = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.activateAbility(player2, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, titan)).isTrue();

        castDrachNyen(titan.getId());
        equipDrachNyen(host);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(titan.getOriginalCard());
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(12);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(2);
    }

    @Test
    void keepsTheExileLinkWhenControlChangesBeforeTheTriggerResolves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent host = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new DrachNyen(), "{4}{B}{R}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        Permanent drachNyen = findPermanent(player1, "Drach'Nyen");

        gd.playerBattlefields.get(player1.getId()).remove(drachNyen);
        gd.playerBattlefields.get(player2.getId()).add(drachNyen);
        resolveAllTriggers();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getOriginalCard());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(drachNyen),
                null, host.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, host, Keyword.MENACE)).isTrue();
    }

    private void addDrachNyenMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
