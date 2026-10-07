package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.d.Distress;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrafdiggersCage;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TergridGodOfFright.class, CruelEdict.class, Distress.class, GrizzlyBears.class, Shock.class,
        Pacifism.class, Forest.class, GrafdiggersCage.class})
class TergridGodOfFrightTest extends BaseCardTest {

    @Test
    void returnsAnOpponentsDiscardedPermanentUnderItsControl() {
        harness.addToBattlefield(player1, new TergridGodOfFright());
        GrizzlyBears discarded = new GrizzlyBears();
        harness.setHand(player2, List.of(discarded));
        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void returnsAnOpponentsSacrificedNontokenPermanentUnderItsControl() {
        harness.addToBattlefield(player1, new TergridGodOfFright());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void doesNotTriggerWhenAnOpponentsPermanentIsDestroyed() {
        harness.addToBattlefield(player1, new TergridGodOfFright());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void canCastAndActivateTheBackFace() {
        TergridGodOfFright card = new TergridGodOfFright();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();

        Permanent lantern = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player2, List.of());
        harness.activateAbility(player1, 0, 0, player2.getId(), null);
        harness.passBothPriorities();

        assertThat(lantern.isTapped()).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);

        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(lantern.isTapped()).isFalse();
    }

    @Test
    void mayDeclineToReturnADiscardedPermanent() {
        harness.addToBattlefield(player1, new TergridGodOfFright());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void doesNotTriggerForADiscardedNonpermanentCard() {
        harness.addToBattlefield(player1, new TergridGodOfFright());
        harness.setHand(player2, List.of(new Shock()));
        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player2, "Shock");
        harness.assertNotOnBattlefield(player1, "Shock");
    }

    @Test
    void choosesWhatADiscardedAuraEnchantsBeforeItEnters() {
        harness.addToBattlefield(player1, new TergridGodOfFright());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Pacifism()));
        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        var enchantedId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, enchantedId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Pacifism");
        harness.assertNotInGraveyard(player2, "Pacifism");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Pacifism")).findFirst().orElseThrow()
                .getAttachedTo()).isEqualTo(enchantedId);
    }

    @Test
    void lanternLetsTargetSacrificeInsteadOfLosingLife() {
        castLantern();
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, player2.getId(), null);
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Sacrifice a nonland permanent");
        harness.handlePermanentChosen(player2, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void lanternLetsTargetDiscardInsteadOfLosingLife() {
        castLantern();
        harness.setHand(player2, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, player2.getId(), null);
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Discard a card");
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
    }

    @Test
    void lanternLetsTargetLoseLifeDespiteOtherAvailableChoices() {
        castLantern();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));

        harness.activateAbility(player1, 0, 0, player2.getId(), null);
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Lose 3 life");
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Shock");
    }

    @Test
    void lanternCanTargetItsControllerAndSacrificeItself() {
        castLantern();
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 0, player1.getId(), null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Sacrifice a nonland permanent");
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Tergrid's Lantern"));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertNotOnBattlefield(player1, "Tergrid's Lantern");
        harness.assertInGraveyard(player1, "Tergrid, God of Fright");
    }

    private void castLantern() {
        harness.setHand(player1, List.of(new TergridGodOfFright()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();
    }

    @Test
    void canCastTheFrontFace() {
        harness.setHand(player1, List.of(new TergridGodOfFright()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tergrid, God of Fright");
        harness.assertNotOnBattlefield(player1, "Tergrid's Lantern");
    }

    @Test
    void doesNotTriggerForItsControllersDiscard() {
        harness.addToBattlefield(player1, new TergridGodOfFright());
        harness.setHand(player1, List.of(new Distress(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void lanternCannotSacrificeALandToAvoidLifeLoss() {
        castLantern();
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player2, new Forest());

        harness.activateAbility(player1, 0, 0, player2.getId(), null);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void returnsALandDiscardedToTheLantern() {
        harness.addToBattlefield(player1, new TergridGodOfFright());
        castLantern();
        harness.setHand(player2, List.of(new Forest()));

        harness.activateAbility(player1, 1, 0, player2.getId(), null);
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Discard a card");
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotInGraveyard(player2, "Forest");
    }

    @Test
    void aCreatureBlockedFromReturningStaysInItsOwnersGraveyard() {
        harness.addToBattlefield(player1, new TergridGodOfFright());
        harness.addToBattlefield(player1, new GrafdiggersCage());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }
}
