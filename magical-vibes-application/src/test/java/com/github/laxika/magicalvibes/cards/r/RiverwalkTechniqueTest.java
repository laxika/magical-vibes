package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FireRimForm;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SibsigAppraiser;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiverwalkTechnique.class, SibsigAppraiser.class, Island.class, FireRimForm.class})
class RiverwalkTechniqueTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Puts target nonland permanent on top or bottom of its owner's library")
    @CardUsed({RiverwalkTechnique.class, SibsigAppraiser.class, Island.class, FireRimForm.class})
    class LibraryMode {

        @Test
        @DisplayName("The permanent's owner can choose the top of their library")
        void ownerChoosesTop() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new SibsigAppraiser());
            Card libraryCard = new Island();
            harness.setLibrary(player2, List.of(libraryCard));

            castMode(0, target.getId());
            assertThat(gd.interaction.activeInteraction())
                    .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);

            harness.handleListChoice(player2, "Top");

            assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard(), libraryCard);
            harness.assertNotOnBattlefield(player2, "Sibsig Appraiser");
        }

        @Test
        @DisplayName("The permanent's owner can choose the bottom of their library")
        void ownerChoosesBottom() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new SibsigAppraiser());
            Card libraryCard = new Island();
            harness.setLibrary(player2, List.of(libraryCard));

            castMode(0, target.getId());
            harness.handleListChoice(player2, "Bottom");

            assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard, target.getCard());
            harness.assertNotOnBattlefield(player2, "Sibsig Appraiser");
        }

        @Test
        @DisplayName("A stolen permanent's owner chooses its destination in their own library")
        void ownerChoosesForStolenPermanent() {
            SibsigAppraiser creature = new SibsigAppraiser();
            creature.setOwnerId(player2.getId());
            Permanent target = harness.addToBattlefieldAndReturn(player1, creature);
            Card ownerLibraryCard = new Island();
            Card controllerLibraryCard = new Island();
            harness.setLibrary(player2, List.of(ownerLibraryCard));
            harness.setLibrary(player1, List.of(controllerLibraryCard));

            castMode(0, target.getId());

            assertThat(gd.interaction.activeInteraction(PendingInteraction.TargetLibraryDestinationChoice.class)
                    .playerId()).isEqualTo(player2.getId());
            assertThatThrownBy(() -> harness.handleListChoice(player1, "Bottom"))
                    .isInstanceOf(IllegalStateException.class);
            harness.handleListChoice(player2, "Bottom");

            assertThat(gd.playerDecks.get(player2.getId())).containsExactly(ownerLibraryCard, creature);
            assertThat(gd.playerDecks.get(player1.getId())).containsExactly(controllerLibraryCard);
            harness.assertNotOnBattlefield(player1, "Sibsig Appraiser");
            harness.assertInGraveyard(player1, "Riverwalk Technique");
        }

        @Test
        @DisplayName("Can put a permanent you own and control on top of your library")
        void canTargetOwnPermanent() {
            Permanent target = harness.addToBattlefieldAndReturn(player1, new SibsigAppraiser());
            Card libraryCard = new Island();
            harness.setLibrary(player1, List.of(libraryCard));

            castMode(0, target.getId());
            harness.handleListChoice(player1, "Top");

            assertThat(gd.playerDecks.get(player1.getId())).containsExactly(target.getCard(), libraryCard);
            harness.assertNotOnBattlefield(player1, "Sibsig Appraiser");
            harness.assertInGraveyard(player1, "Riverwalk Technique");
        }

        @Test
        @DisplayName("Bottom is a valid choice with an empty library")
        void canChooseBottomWithEmptyLibrary() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new SibsigAppraiser());
            harness.setLibrary(player2, List.of());

            castMode(0, target.getId());
            harness.handleListChoice(player2, "Bottom");

            assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard());
            harness.assertNotOnBattlefield(player2, "Sibsig Appraiser");
            harness.assertInGraveyard(player1, "Riverwalk Technique");
        }

        @Test
        @DisplayName("Can put a noncreature permanent into its owner's library")
        void canTargetEnchantment() {
            Permanent creature = harness.addToBattlefieldAndReturn(player2, new SibsigAppraiser());
            Permanent aura = harness.addToBattlefieldAndReturn(player2, new FireRimForm());
            aura.setAttachedTo(creature.getId());
            Card libraryCard = new Island();
            harness.setLibrary(player2, List.of(libraryCard));

            castMode(0, aura.getId());
            harness.handleListChoice(player2, "Bottom");

            assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard, aura.getCard());
            harness.assertNotOnBattlefield(player2, "Fire-Rim Form");
            harness.assertOnBattlefield(player2, "Sibsig Appraiser");
        }

        @Test
        @DisplayName("Does not move a target that has already left the battlefield")
        void doesNotResolveForDepartedTarget() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new SibsigAppraiser());
            Card libraryCard = new Island();
            harness.setLibrary(player2, List.of(libraryCard));
            prepareCard();
            harness.castInstant(player1, 0, 0, target.getId());
            harness.passPriority(player1);
            harness.setHand(player2, List.of(new RiverwalkTechnique()));
            harness.addMana(player2, ManaColor.BLUE, 1);
            harness.addMana(player2, ManaColor.COLORLESS, 3);
            harness.castInstant(player2, 0, 0, target.getId());
            harness.passBothPriorities();
            harness.handleListChoice(player2, "Bottom");
            harness.passBothPriorities();

            assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard, target.getCard());
            assertThat(gd.interaction.activeInteraction()).isNull();
            assertThat(gd.stack).isEmpty();
            harness.assertInGraveyard(player1, "Riverwalk Technique");
            harness.assertInGraveyard(player2, "Riverwalk Technique");
        }

        @Test
        @DisplayName("Cannot target a land")
        void cannotTargetLand() {
            Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
            prepareCard();

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, land.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Counters target noncreature spell")
    @CardUsed({RiverwalkTechnique.class, SibsigAppraiser.class, FireRimForm.class})
    class CounterMode {

        @Test
        @DisplayName("Counters a noncreature spell")
        void countersNoncreatureSpell() {
            FireRimForm aura = new FireRimForm();
            Permanent target = harness.addToBattlefieldAndReturn(player2, new SibsigAppraiser());
            harness.forceActivePlayer(player2);
            harness.setHand(player2, List.of(aura));
            harness.addMana(player2, ManaColor.RED, 1);
            harness.addMana(player2, ManaColor.COLORLESS, 1);
            prepareCard();

            harness.castInstant(player2, 0, target.getId());
            harness.passPriority(player2);
            harness.castInstant(player1, 0, 1, aura.getId());
            harness.passBothPriorities();

            harness.assertInGraveyard(player2, "Fire-Rim Form");
            harness.assertInGraveyard(player1, "Riverwalk Technique");
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Counters an instant before its library mode resolves")
        void countersInstant() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new SibsigAppraiser());
            RiverwalkTechnique opposingSpell = new RiverwalkTechnique();
            harness.forceActivePlayer(player2);
            harness.setHand(player2, List.of(opposingSpell));
            harness.addMana(player2, ManaColor.BLUE, 1);
            harness.addMana(player2, ManaColor.COLORLESS, 3);
            prepareCard();

            harness.castInstant(player2, 0, 0, target.getId());
            harness.passPriority(player2);
            harness.castInstant(player1, 0, 1, opposingSpell.getId());
            harness.passBothPriorities();

            harness.assertOnBattlefield(player2, "Sibsig Appraiser");
            harness.assertInGraveyard(player2, "Riverwalk Technique");
            harness.assertInGraveyard(player1, "Riverwalk Technique");
            assertThat(gd.interaction.activeInteraction()).isNull();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Cannot counter a triggered ability")
        void cannotTargetTriggeredAbility() {
            harness.forceActivePlayer(player2);
            harness.castFromHand(player2, new SibsigAppraiser(), "{2}{U}");
            prepareCard();
            harness.passBothPriorities();
            assertThat(gd.stack).hasSize(1);
            java.util.UUID abilityId = gd.stack.getLast().getTargetableId();

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, abilityId))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Cannot target a creature spell")
        void cannotTargetCreatureSpell() {
            SibsigAppraiser creature = new SibsigAppraiser();
            harness.forceActivePlayer(player2);
            harness.castFromHand(player2, creature, "{2}{U}");
            prepareCard();

            harness.passPriority(player2);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, creature.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    private void castMode(int mode, java.util.UUID targetId) {
        prepareCard();
        harness.castInstant(player1, 0, mode, targetId);
        harness.passBothPriorities();
    }

    private void prepareCard() {
        harness.setHand(player1, List.of(new RiverwalkTechnique()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
