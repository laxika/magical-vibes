package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KitsaOtterballElite;
import com.github.laxika.magicalvibes.cards.p.PearlOfWisdom;
import com.github.laxika.magicalvibes.cards.r.RunAwayTogether;
import com.github.laxika.magicalvibes.cards.s.ShoreUp;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlaniaDivergentStorm.class, Divination.class, GrizzlyBears.class, KitsaOtterballElite.class,
        Shock.class, AlaniasPathmaker.class, PearlOfWisdom.class, RunAwayTogether.class, ShoreUp.class})
class AlaniaDivergentStormTest extends BaseCardTest {

    @Test
    @DisplayName("Draws for the target opponent before copying the first instant")
    void drawsThenCopiesFirstInstant() {
        harness.addToBattlefield(player1, new AlaniaDivergentStorm());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(8);
        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
    }

    @Test
    @DisplayName("Uses separate first-spell conditions for instants, sorceries, and Otters")
    void usesIndependentFirstSpellConditions() {
        harness.addToBattlefield(player1, new AlaniaDivergentStorm());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Divination(), new KitsaOtterballElite(),
                new KitsaOtterballElite()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castInstant(player1, 0, player2.getId());
        declineTriggerAndResolveSpell();

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.passBothPriorities();

        harness.castSorcery(player1, 0, 0);
        declineTriggerAndResolveSpell();

        harness.castCreature(player1, 0);
        declineTriggerAndResolveSpell();

        harness.castCreature(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("Declining does not draw for the opponent or copy the spell")
    void decliningDoesNotDrawOrCopy() {
        Permanent alania = harness.addToBattlefieldAndReturn(player1, new AlaniaDivergentStorm());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new ShoreUp()));
        harness.setHand(player1, List.of(new ShoreUp()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, alania.getId());
        declineTriggerAndResolveSpell();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Copies the first sorcery and resolves both copies' effects")
    void copiesFirstSorcery() {
        harness.addToBattlefield(player1, new AlaniaDivergentStorm());
        harness.setHand(player1, List.of(new PearlOfWisdom()));
        harness.setLibrary(player1, List.of(new ShoreUp(), new ShoreUp(), new ShoreUp(), new ShoreUp()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new ShoreUp()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0);
        acceptTriggerAndCreateCopy();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An Otter spell copy resolves as a token before the original")
    void otterCopyResolvesAsToken() {
        harness.addToBattlefield(player1, new AlaniaDivergentStorm());
        harness.setHand(player1, List.of(new AlaniasPathmaker()));
        harness.setLibrary(player1, List.of(new ShoreUp(), new ShoreUp()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new ShoreUp()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        acceptTriggerAndCreateCopy();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Alania's Pathmaker"))
                .singleElement().satisfies(permanent -> assertThat(permanent.getCard().isToken()).isTrue());
        assertThat(gd.stack).filteredOn(entry -> entry.getEntryType() == StackEntryType.CREATURE_SPELL)
                .singleElement().satisfies(entry -> assertThat(entry.isCopy()).isFalse());
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The copy can use a new target while the original retains its target")
    void canRetargetSingleTargetCopy() {
        Permanent alania = harness.addToBattlefieldAndReturn(player1, new AlaniaDivergentStorm());
        Permanent pathmaker = harness.addToBattlefieldAndReturn(player1, new AlaniasPathmaker());
        alania.tap();
        pathmaker.tap();
        harness.setHand(player1, List.of(new ShoreUp()));
        harness.setLibrary(player2, List.of(new ShoreUp()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, alania.getId());
        acceptTriggerAndCreateCopy();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, pathmaker.getId());
        harness.passBothPriorities();

        assertThat(pathmaker.isTapped()).isFalse();
        assertThat(alania.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(alania.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An instant cast before Alania enters still consumes the first-instant condition")
    void countsSpellsCastBeforeAlaniaEntered() {
        Permanent pathmaker = harness.addToBattlefieldAndReturn(player1, new AlaniasPathmaker());
        harness.setHand(player1, List.of(new ShoreUp(), new ShoreUp()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, pathmaker.getId());
        harness.addToBattlefield(player1, new AlaniaDivergentStorm());

        harness.castInstant(player1, 0, pathmaker.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("Casting Alania does not consume the first other-Otter condition")
    void excludesAlaniaFromOtherOtterCount() {
        harness.setHand(player1, List.of(new AlaniaDivergentStorm(), new KitsaOtterballElite()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        declineTriggerAndResolveSpell();

        harness.assertOnBattlefield(player1, "Kitsa, Otterball Elite");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A spell with two targets offers the choice to change the copy's targets")
    void canRetargetMultipleTargetCopy() {
        Permanent alania = harness.addToBattlefieldAndReturn(player1, new AlaniaDivergentStorm());
        harness.addToBattlefield(player1, new AlaniasPathmaker());
        Permanent opponentPathmaker = harness.addToBattlefieldAndReturn(player2, new AlaniasPathmaker());
        harness.addToBattlefield(player2, new KitsaOtterballElite());
        harness.setHand(player1, List.of(new RunAwayTogether()));
        harness.setLibrary(player2, List.of(new ShoreUp()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, List.of(alania.getId(), opponentPathmaker.getId()));
        acceptTriggerAndCreateCopy();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    private void acceptTriggerAndCreateCopy() {
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }

    private void declineTriggerAndResolveSpell() {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
    }
}
