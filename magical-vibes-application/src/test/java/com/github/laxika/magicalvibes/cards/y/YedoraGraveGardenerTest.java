package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HallowedMoonlight;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YedoraGraveGardener.class, GrizzlyBears.class, HallowedMoonlight.class})
class YedoraGraveGardenerTest extends BaseCardTest {

    @Test
    void mayDeclineReturningCreature() {
        harness.addToBattlefield(player1, new YedoraGraveGardener());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void doesNotTriggerForItsOwnDeath() {
        Permanent yedora = harness.addToBattlefieldAndReturn(player1, new YedoraGraveGardener());
        yedora.setMarkedDamage(5);

        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Yedora, Grave Gardener");
    }

    @Test
    void doesNotTriggerForOpponentsCreature() {
        harness.addToBattlefield(player1, new YedoraGraveGardener());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setMarkedDamage(2);

        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void doesNotTriggerForTokenCreature() {
        harness.addToBattlefield(player1, new YedoraGraveGardener());
        GrizzlyBears token = new GrizzlyBears();
        token.setToken(true);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, token);
        bears.setMarkedDamage(2);

        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void returnsCreatureToOwnerRatherThanController() {
        harness.addToBattlefield(player1, new YedoraGraveGardener());
        GrizzlyBears card = new GrizzlyBears();
        card.setOwnerId(player2.getId());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, card);
        bears.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard().getId()).isEqualTo(card.getId());
                    assertThat(permanent.isFaceDown()).isTrue();
                    assertThat(gqs.getEffectiveCardTypes(gd, permanent)).containsExactly(CardType.LAND);
                });
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(card.getId()));
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void triggersWhenYedoraAndAnotherCreatureDieSimultaneously() {
        Permanent yedora = harness.addToBattlefieldAndReturn(player1, new YedoraGraveGardener());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        yedora.setMarkedDamage(5);
        bears.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Yedora, Grave Gardener");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(permanent -> {
                    assertThat(permanent.getCard().getId()).isEqualTo(bears.getCard().getId());
                    assertThat(permanent.isFaceDown()).isTrue();
                });
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotReturnCreatureThatLeftGraveyard() {
        harness.addToBattlefield(player1, new YedoraGraveGardener());
        GrizzlyBears card = new GrizzlyBears();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, card);
        bears.setMarkedDamage(2);
        harness.runStateBasedActions();

        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(card));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    @Test
    void oldTriggerCannotReturnCreatureAfterItLeavesGraveyardAndDiesAgain() {
        harness.addToBattlefield(player1, new YedoraGraveGardener());
        GrizzlyBears card = new GrizzlyBears();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, card);
        bears.setMarkedDamage(2);
        harness.runStateBasedActions();

        harness.setGraveyard(player1, List.of());
        Permanent reanimated = harness.enterBattlefieldAndReturn(player1, card);
        reanimated.setMarkedDamage(2);
        harness.runStateBasedActions();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void hallowedMoonlightDoesNotExileReturningForest() {
        harness.setHand(player1, List.of(new HallowedMoonlight()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0);
        harness.addToBattlefield(player1, new YedoraGraveGardener());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard().getId()).isEqualTo(bears.getCard().getId());
                    assertThat(permanent.isFaceDown()).isTrue();
                    assertThat(gqs.getEffectiveCardTypes(gd, permanent)).containsExactly(CardType.LAND);
                });
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(bears.getCard());
    }

    @Test
    void mayReturnAnotherNontokenCreatureAsFaceDownForest() {
        harness.addToBattlefield(player1, new YedoraGraveGardener());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(bears.getCard().getId()))
                .findFirst().orElseThrow();
        assertThat(returned.isFaceDown()).isTrue();
        assertThat(gqs.getEffectiveCardTypes(gd, returned)).containsExactly(CardType.LAND);
        assertThat(gqs.hasEffectiveSubtype(gd, returned, CardSubtype.FOREST)).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(gqs.isLand(gd, returned)).isTrue();

        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(returned));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }
}
