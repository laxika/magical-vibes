package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConquerorsGalleon.class, ConquerorsFoothold.class, GrizzlyBears.class, Shock.class, ColossalDreadmaw.class})
class ConquerorsGalleonTest extends BaseCardTest {

    @Nested
    @DisplayName("Attack and transform")
    @CardUsed({ConquerorsGalleon.class, ConquerorsFoothold.class})
    class AttackAndTransform {

        @Test
        @DisplayName("Attacking with Galleon exiles it at end of combat and returns transformed as Foothold")
        void attackExilesAndReturnsTransformed() {
            Permanent galleon = addGalleonReady(player1);
            animateAsCreature(galleon);

            declareAttackers(List.of(0));
            // Resolve the ON_ATTACK trigger (schedules exile at end of combat)
            harness.passBothPriorities();

            // Advance to END_OF_COMBAT
            harness.forceStep(TurnStep.END_OF_COMBAT);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            // Galleon should be gone, Foothold should be on the battlefield
            harness.assertNotOnBattlefield(player1, "Conqueror's Galleon");
            harness.assertOnBattlefield(player1, "Conqueror's Foothold");

            // The new permanent should be marked as transformed
            Permanent foothold = findPermanent(player1, "Conqueror's Foothold");
            assertThat(foothold.isTransformed()).isTrue();
        }

        @Test
        @DisplayName("If Galleon leaves the battlefield before end of combat, it does not return")
        void noReturnIfAlreadyGone() {
            Permanent galleon = addGalleonReady(player1);
            animateAsCreature(galleon);

            declareAttackers(List.of(0));
            // Resolve the ON_ATTACK trigger
            harness.passBothPriorities();

            // Remove the Galleon before end of combat (e.g. destroyed in combat)
            gd.playerBattlefields.get(player1.getId()).clear();

            // Advance to END_OF_COMBAT
            harness.forceStep(TurnStep.END_OF_COMBAT);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            // No permanents should be on the battlefield
            assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        }

        @Test
        @DisplayName("Foothold returns under controller's control even if Galleon was stolen")
        void returnsUnderControllerControl() {
            ConquerorsGalleon card = new ConquerorsGalleon();
            card.setOwnerId(player2.getId());
            Permanent galleon = addCreatureReady(player1, card);
            animateAsCreature(galleon);

            declareAttackers(List.of(0));
            harness.passBothPriorities();

            // Advance to END_OF_COMBAT
            harness.forceStep(TurnStep.END_OF_COMBAT);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            // Foothold should be on player1's battlefield
            harness.assertOnBattlefield(player1, "Conqueror's Foothold");
            harness.assertNotOnBattlefield(player2, "Conqueror's Foothold");
        }
    }

    @Nested
    @DisplayName("Conqueror's Foothold abilities")
    @CardUsed({ConquerorsGalleon.class, ConquerorsFoothold.class, GrizzlyBears.class, Shock.class})
    class FootholdAbilities {

        @Test
        @DisplayName("{2}, {T}: Draw a card, then discard a card (loot)")
        void lootAbility() {
            Permanent foothold = addFootholdReady(player1);
            harness.setLibrary(player1, List.of(new GrizzlyBears()));
            harness.setHand(player1, new ArrayList<>(List.of(new Shock())));

            harness.addMana(player1, ManaColor.COLORLESS, 2);
            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();

            // Drew a card, now awaiting discard choice
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
            harness.handleCardChosen(player1, 0);

            // Hand size should be 1 (started with 1, drew 1, discarded 1)
            assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
            assertThat(foothold.isTapped()).isTrue();
        }

        @Test
        @DisplayName("{4}, {T}: Draw a card")
        void drawAbility() {
            Permanent foothold = addFootholdReady(player1);
            harness.setLibrary(player1, List.of(new GrizzlyBears()));
            harness.setHand(player1, new ArrayList<>());

            harness.addMana(player1, ManaColor.COLORLESS, 4);
            harness.activateAbility(player1, 0, 1, null, null);
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
            assertThat(foothold.isTapped()).isTrue();
        }

        @Test
        @DisplayName("{6}, {T}: Return target card from graveyard to hand")
        void returnFromGraveyardAbility() {
            Permanent foothold = addFootholdReady(player1);
            Card bears = new GrizzlyBears();
            harness.setGraveyard(player1, List.of(bears));

            harness.addMana(player1, ManaColor.COLORLESS, 6);
            harness.activateAbility(player1, 0, 2, null, bears.getId(), Zone.GRAVEYARD);
            harness.passBothPriorities();

            harness.assertInHand(player1, "Grizzly Bears");
            harness.assertNotInGraveyard(player1, "Grizzly Bears");
            assertThat(foothold.isTapped()).isTrue();
        }
    }

    @Test
    void crewAnimatesVehicleAndTapsCrew() {
        Permanent galleon = addGalleonReady(player1);
        Permanent crew = addCreatureReady(player1, new ColossalDreadmaw());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, galleon)).isTrue();
        assertThat(galleon.isTapped()).isFalse();
    }

    @Test
    void cannotCrewWithOnlyTwoPower() {
        addGalleonReady(player1);
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void footholdAddsColorlessManaImmediately() {
        Permanent foothold = addFootholdReady(player1);

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(foothold.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void footholdCanReturnNoncreatureCard() {
        addFootholdReady(player1);
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 2, null, shock.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Shock");
        harness.assertNotInGraveyard(player1, "Shock");
    }

    @Test
    void endOfCombatReturnUsesDelayedTriggerOnStack() {
        Permanent galleon = addGalleonReady(player1);
        animateAsCreature(galleon);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        harness.withAutoStop(TurnStep.END_OF_COMBAT,
                () -> harness.passUntil(TurnStep.END_OF_COMBAT));

        harness.assertOnBattlefield(player1, "Conqueror's Galleon");
        harness.assertNotOnBattlefield(player1, "Conqueror's Foothold");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Conqueror's Foothold");
        Permanent foothold = findPermanent(player1, "Conqueror's Foothold");
        assertThat(foothold.getId()).isNotEqualTo(galleon.getId());
        assertThat(foothold.isTapped()).isFalse();
    }

    private Permanent addGalleonReady(Player player) {
        return addCreatureReady(player, new ConquerorsGalleon());
    }

    private Permanent addFootholdReady(Player player) {
        ConquerorsGalleon galleon = new ConquerorsGalleon();
        Permanent perm = addCreatureReady(player, galleon);
        perm.setCard(galleon.getBackFaceCard());
        perm.setTransformed(true);
        return perm;
    }

    private void animateAsCreature(Permanent perm) {
        perm.setAnimatedUntilEndOfTurn(true);
        perm.setAnimatedPower(2);
        perm.setAnimatedToughness(10);
    }

}
