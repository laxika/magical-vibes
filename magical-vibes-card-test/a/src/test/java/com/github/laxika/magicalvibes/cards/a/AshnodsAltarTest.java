package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.ForceSpike;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GravebaneZombie;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KalitasTraitorOfGhet;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FailOnLegacyDepartureFallback;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AshnodsAltar.class, GrizzlyBears.class, AnodetLurker.class})
class AshnodsAltarTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature adds two colorless mana")
    void sacrificeAddsTwoColorlessMana() {
        harness.addToBattlefield(player1, new AshnodsAltar());
        Permanent sacrificedBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, sacrificedBear.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();

        // One Grizzly Bears is sacrificed to the graveyard
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Grizzly Bears"))
                .hasSize(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A death trigger from the chosen sacrifice goes on the stack once the activation completes")
    void deathTriggerFromChosenSacrificeGoesOnStackAfterActivation() {
        harness.addToBattlefield(player1, new AshnodsAltar());
        Permanent lurker = harness.addToBattlefieldAndReturn(player1, new AnodetLurker());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, lurker.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.pendingManaAbilityTriggers).isEmpty();
        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getCard().getName()).isEqualTo("Anodet Lurker");
        });
        harness.passBothPriorities();
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Automatically sacrifices the only creature available")
    void automaticallySacrificesOnlyCreatureAvailable() {
        harness.addToBattlefield(player1, new AshnodsAltar());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bear);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bear.getCard());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without a creature controlled by the activator")
    void cannotActivateWithoutControllerCreature() {
        harness.addToBattlefield(player1, new AshnodsAltar());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Altar can sacrifice a tapped creature that just entered")
    void tappedAltarCanSacrificeTappedNewCreature() {
        Permanent altar = harness.addToBattlefieldAndReturn(player1, new AshnodsAltar());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        altar.tap();
        bear.tap();
        bear.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(altar);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bear.getCard());
        assertThat(altar.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The Altar can be activated repeatedly, sacrificing a different creature each time")
    void repeatedActivationsEachRequireCreature() {
        Permanent altar = harness.addToBattlefieldAndReturn(player1, new AshnodsAltar());
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, firstBear.getId());
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(altar);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(firstBear.getCard(), secondBear.getCard());
        assertThat(altar.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @FailOnLegacyDepartureFallback
    @CardUsed({KalitasTraitorOfGhet.class, GravebaneZombie.class})
    @DisplayName("Sacrificing Gravebane Zombie under an opponent's Kalitas asks library-or-Kalitas before any mana is added")
    void sacrificeAsksTheDeathReplacementBeforeAddingMana() {
        harness.addToBattlefield(player2, new KalitasTraitorOfGhet());
        harness.addToBattlefield(player1, new AshnodsAltar());
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new GravebaneZombie());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, zombie.getId());

        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player1, "Gravebane Zombie");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.handleListChoice(player1, "Kalitas, Traitor of Ghet");

        harness.assertNotOnBattlefield(player1, "Gravebane Zombie");
        assertThat(gd.findExiledCard(zombie.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @FailOnLegacyDepartureFallback
    @CardUsed({KalitasTraitorOfGhet.class, GravebaneZombie.class, ForceSpike.class, LlanowarElves.class, Forest.class})
    @DisplayName("Activated while paying Force Spike's cost, the death replacement question comes before the payment prompt returns")
    void deathReplacementQuestionNestsInsideAPaymentPrompt() {
        harness.addToBattlefield(player2, new KalitasTraitorOfGhet());
        harness.addToBattlefield(player1, new AshnodsAltar());
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new GravebaneZombie());
        harness.addToBattlefield(player1, new Forest());
        LlanowarElves elves = new LlanowarElves();
        harness.castFromHand(player1, elves, "{G}");
        harness.setHand(player2, List.of(new ForceSpike()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elves.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        gs.activateAbility(gd, player1, 0, null, null, null, null);

        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player1, "Gravebane Zombie");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.handleListChoice(player1, "Put it into its owner's library");

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(zombie.getCard().getId());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }
}

