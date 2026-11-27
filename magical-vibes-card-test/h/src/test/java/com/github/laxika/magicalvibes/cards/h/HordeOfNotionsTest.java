package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NamelessInversion;
import com.github.laxika.magicalvibes.cards.p.ProtectiveBubble;
import com.github.laxika.magicalvibes.cards.s.Smokebraider;
import com.github.laxika.magicalvibes.cards.t.ThornOfAmethyst;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.github.laxika.magicalvibes.cards.t.TeferiTimeRaveler;
import com.github.laxika.magicalvibes.model.CounterType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HordeOfNotions.class, Smokebraider.class, GrizzlyBears.class,
        AvianChangeling.class, NamelessInversion.class, ProtectiveBubble.class, ThornOfAmethyst.class,
        Forest.class})
class HordeOfNotionsTest extends BaseCardTest {

    private Permanent addReadyHorde() {
        return addCreatureReady(player1, new HordeOfNotions());
    }

    private void addWubrg(int copies) {
        for (ManaColor color : List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK,
                ManaColor.RED, ManaColor.GREEN)) {
            harness.addMana(player1, color, copies);
        }
    }

    @Test
    @DisplayName("Plays a targeted Elemental card from own graveyard without paying its mana cost")
    void playsElementalFromGraveyard() {
        addReadyHorde();
        Smokebraider elemental = new Smokebraider();
        harness.setGraveyard(player1, List.of(elemental));
        addWubrg(1);

        harness.activateAbility(player1, 0, 0, null, elemental.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities(); // resolve ability → queues may-play

        harness.handleMayAbilityChosen(player1, true);

        // Cast without paying → creature spell on the stack, no longer in graveyard.
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.CREATURE_SPELL
                && e.getCard() == elemental);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();

        harness.passBothPriorities(); // resolve the creature spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == elemental);
    }

    @Test
    @DisplayName("An Elemental land card is played from the graveyard with the turn's land play")
    void playsElementalLandWithTheLandPlay() {
        addReadyHorde();
        Forest land = elementalLand();
        harness.setGraveyard(player1, List.of(land));
        addWubrg(1);

        harness.activateAbility(player1, 0, 0, null, land.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getCard() == land);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("An Elemental land card stays in the graveyard when no land play is left")
    void elementalLandNeedsALandPlay() {
        addReadyHorde();
        Forest land = elementalLand();
        harness.setGraveyard(player1, List.of(land));
        gd.landsPlayedThisTurn.put(player1.getId(), 1);
        addWubrg(1);

        harness.activateAbility(player1, 0, 0, null, land.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard() == land);
    }

    @Test
    @DisplayName("An Elemental land card can't be played from the graveyard during an opponent's turn")
    void elementalLandCantBePlayedOnAnOpponentsTurn() {
        addReadyHorde();
        Forest land = elementalLand();
        harness.setGraveyard(player1, List.of(land));
        addWubrg(1);
        harness.forceActivePlayer(player2);

        harness.activateAbility(player1, 0, 0, null, land.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard() == land);
    }

    private static Forest elementalLand() {
        Forest land = new Forest();
        land.setSubtypes(List.of(CardSubtype.FOREST, CardSubtype.ELEMENTAL));
        return land;
    }

    @Test
    @DisplayName("Declining the may-play leaves the Elemental in the graveyard")
    void decliningLeavesCardInGraveyard() {
        addReadyHorde();
        Smokebraider elemental = new Smokebraider();
        harness.setGraveyard(player1, List.of(elemental));
        addWubrg(1);

        harness.activateAbility(player1, 0, 0, null, elemental.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(elemental);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() == elemental);
    }

    @Test
    @DisplayName("Cannot target a non-Elemental card in the graveyard")
    void cannotTargetNonElemental() {
        addReadyHorde();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        addWubrg(1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an Elemental card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        addReadyHorde();
        Smokebraider elemental = new Smokebraider();
        harness.setGraveyard(player2, List.of(elemental));
        addWubrg(1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, elemental.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Changeling creature cards qualify as Elementals in the graveyard")
    void playsChangelingCreature() {
        addReadyHorde();
        AvianChangeling card = new AvianChangeling();
        harness.setGraveyard(player1, List.of(card));
        addWubrg(1);
        harness.activateAbility(player1, 0, 0, null, card.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getCard() == card);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A changeling instant can be cast with a target and returns to the graveyard")
    void castsChangelingInstant() {
        addReadyHorde();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Smokebraider());
        NamelessInversion spell = new NamelessInversion();
        harness.setGraveyard(player1, List.of(spell));
        addWubrg(1);
        harness.activateAbility(player1, 0, 0, null, spell.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.INSTANT_SPELL
                && e.getCard() == spell);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Smokebraider");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
    }

    @Test
    @DisplayName("The ability does nothing if its target leaves the graveyard")
    void targetLeavingGraveyardMakesAbilityFizzle() {
        addReadyHorde();
        Smokebraider card = new Smokebraider();
        harness.setGraveyard(player1, List.of(card));
        addWubrg(1);
        harness.activateAbility(player1, 0, 0, null, card.getId(), Zone.GRAVEYARD);
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(card));
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard() == card);
    }

    @Test
    @DisplayName("Casting without paying the mana cost still pays Thorn of Amethyst's increase")
    void paysSpellCostIncrease() {
        addReadyHorde();
        harness.addToBattlefield(player2, new ThornOfAmethyst());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Smokebraider());
        NamelessInversion spell = new NamelessInversion();
        harness.setGraveyard(player1, List.of(spell));
        addWubrg(1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, spell.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.INSTANT_SPELL
                && e.getCard() == spell);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Smokebraider");
    }

    @Test
    @DisplayName("The cost increase may be paid with mana from a land tapped while its payment is asked for")
    void paysCostIncreaseWithManaAbilityDuringThePrompt() {
        addReadyHorde();
        harness.addToBattlefield(player2, new ThornOfAmethyst());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Smokebraider());
        NamelessInversion spell = new NamelessInversion();
        harness.setGraveyard(player1, List.of(spell));
        addWubrg(1);
        harness.activateAbility(player1, 0, 0, null, spell.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(forest));
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.INSTANT_SPELL
                && e.getCard() == spell);
        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An instant cast through Horde cannot target a creature with shroud")
    void cannotCastInstantTargetingShroudedCreature() {
        addReadyHorde();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Smokebraider());
        Permanent bubble = harness.addToBattlefieldAndReturn(player2, new ProtectiveBubble());
        bubble.setAttachedTo(target.getId());
        NamelessInversion spell = new NamelessInversion();
        harness.setGraveyard(player1, List.of(spell));
        addWubrg(1);
        harness.activateAbility(player1, 0, 0, null, spell.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed({TeferiTimeRaveler.class})
    @DisplayName("An opponent's Teferi, Time Raveler stops the cast, and the Elemental stays in the graveyard")
    void opponentsTeferiStopsTheCast() {
        addReadyHorde();
        harness.addToBattlefieldAndReturn(player2, new TeferiTimeRaveler()).setCounterCount(CounterType.LOYALTY, 4);
        Smokebraider elemental = new Smokebraider();
        harness.setGraveyard(player1, List.of(elemental));
        addWubrg(1);

        harness.activateAbility(player1, 0, 0, null, elemental.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).noneMatch(e -> e.getCard() == elemental);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(elemental);
    }
}
