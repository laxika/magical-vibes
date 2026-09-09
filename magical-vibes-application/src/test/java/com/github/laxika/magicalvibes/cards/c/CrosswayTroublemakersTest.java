package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrosswayTroublemakers.class, ChildOfNight.class, Forest.class, GrizzlyBears.class, Murder.class})
class CrosswayTroublemakersTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking Vampires you control have deathtouch and lifelink")
    void attackingVampiresYouControlHaveDeathtouchAndLifelink() {
        Permanent source = addReadyCreature(player1, new CrosswayTroublemakers());
        Permanent vampire = addReadyCreature(player1, new ChildOfNight());
        Permanent nonVampire = addReadyCreature(player1, new GrizzlyBears());
        Permanent opponentVampire = addReadyCreature(player2, new CrosswayTroublemakers());

        source.setAttacking(true);
        vampire.setAttacking(true);
        nonVampire.setAttacking(true);

        assertThat(gqs.hasKeyword(gd, source, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, source, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, vampire, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, vampire, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonVampire, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, nonVampire, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentVampire, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentVampire, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Paying 2 life after a Vampire you control dies draws a card")
    void payingLifeAfterVampireDiesDrawsCard() {
        addReadyCreature(player1, new CrosswayTroublemakers());
        harness.addToBattlefield(player1, new ChildOfNight());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLife(player1, 20);

        killPermanent(player1, "Child of Night");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The death ability also triggers when Crossway Troublemakers dies")
    void triggersWhenThisCreatureDies() {
        addReadyCreature(player1, new CrosswayTroublemakers());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLife(player1, 20);

        killPermanent(player1, "Crossway Troublemakers");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Declining the death ability does not cost life or draw")
    void decliningDeathAbilityDoesNothing() {
        addReadyCreature(player1, new CrosswayTroublemakers());
        harness.addToBattlefield(player1, new ChildOfNight());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLife(player1, 20);

        killPermanent(player1, "Child of Night");

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A non-Vampire creature dying does not trigger the death ability")
    void doesNotTriggerForNonVampire() {
        addReadyCreature(player1, new CrosswayTroublemakers());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        killPermanent(player1, "Grizzly Bears");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player,
                                       com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void killPermanent(com.github.laxika.magicalvibes.model.Player controller, String name) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 2);
        harness.addMana(player2, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 1);

        UUID permanentId = harness.getPermanentId(controller, name);
        harness.castInstant(player2, 0, permanentId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
