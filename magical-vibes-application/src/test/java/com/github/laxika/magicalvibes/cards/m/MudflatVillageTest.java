package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BushyBodyguard;
import com.github.laxika.magicalvibes.cards.f.FlamecacheGecko;
import com.github.laxika.magicalvibes.cards.p.PawpatchRecruit;
import com.github.laxika.magicalvibes.cards.p.PersistentMarshstalker;
import com.github.laxika.magicalvibes.cards.s.Savor;
import com.github.laxika.magicalvibes.cards.s.StarscapeCleric;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MudflatVillage.class, StarscapeCleric.class, FlamecacheGecko.class,
        PersistentMarshstalker.class, BushyBodyguard.class, PawpatchRecruit.class, Savor.class})
class MudflatVillageTest extends BaseCardTest {

    @Test
    void tapsForColorless() {
        addVillage();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void addsBlackManaOnlyForCreatureSpells() {
        addVillage();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyMana(ManaColor.BLACK))
                .isEqualTo(1);
    }

    @Test
    void creatureOnlyBlackManaCannotCastNoncreatureSpells() {
        addVillage();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.setHand(player1, List.of(new Savor()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addToBattlefield(player2, new PawpatchRecruit());

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                gd.playerBattlefields.get(player2.getId()).getFirst().getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void returnsTargetedKindredCardFromGraveyardToHandAndSacrificesItself() {
        addVillage();
        Card bat = new StarscapeCleric();
        harness.setGraveyard(player1, List.of(bat));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 2, null, bat.getId(), Zone.GRAVEYARD);
        harness.assertInGraveyard(player1, "Mudflat Village");
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(bat.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(bat.getId()));
    }

    @Test
    void cannotTargetNonKindredCardInGraveyard() {
        addVillage();
        Card bear = new PawpatchRecruit();
        harness.setGraveyard(player1, List.of(bear));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 2, null, bear.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void restrictedBlackManaPaysForCreatureSpell() {
        addVillage();
        harness.setHand(player1, List.of(new StarscapeCleric()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Starscape Cleric");
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyMana(ManaColor.BLACK))
                .isZero();
    }

    @Test
    void returnsLizardCard() {
        assertReturnsToHand(new FlamecacheGecko());
    }

    @Test
    void returnsRatCard() {
        assertReturnsToHand(new PersistentMarshstalker());
    }

    @Test
    void returnsSquirrelCard() {
        assertReturnsToHand(new BushyBodyguard());
    }

    @Test
    void cannotTargetOpponentsEligibleCard() {
        addVillage();
        Card bat = new StarscapeCleric();
        harness.setGraveyard(player2, List.of(bat));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 2, null, bat.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Mudflat Village");
        harness.assertInGraveyard(player2, "Starscape Cleric");
    }

    @Test
    void missingTargetDoesNotReturnAnotherEligibleCard() {
        addVillage();
        Card bat = new StarscapeCleric();
        Card rat = new PersistentMarshstalker();
        harness.setGraveyard(player1, List.of(bat, rat));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 2, null, bat.getId(), Zone.GRAVEYARD);
        gd.playerGraveyards.get(player1.getId()).remove(bat);
        harness.setExile(player1, List.of(bat));

        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Starscape Cleric");
        harness.assertNotInHand(player1, "Persistent Marshstalker");
        harness.assertInGraveyard(player1, "Persistent Marshstalker");
        harness.assertInGraveyard(player1, "Mudflat Village");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void creatureOnlyManaCannotPayForReturnAbility() {
        addVillage();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.addToBattlefield(player1, new MudflatVillage());
        Card bat = new StarscapeCleric();
        harness.setGraveyard(player1, List.of(bat));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 1, 2, null, bat.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Starscape Cleric");
    }

    @Test
    void tappedVillageCannotActivateReturnAbility() {
        addVillage();
        harness.activateAbility(player1, 0, 0, null, null);
        Card bat = new StarscapeCleric();
        harness.setGraveyard(player1, List.of(bat));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 2, null, bat.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Mudflat Village");
        harness.assertInGraveyard(player1, "Starscape Cleric");
    }

    @Test
    void returnAbilityRequiresATarget() {
        addVillage();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 2, null, null, Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Mudflat Village");
    }

    private void assertReturnsToHand(Card target) {
        addVillage();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 2, null, target.getId(), Zone.GRAVEYARD);
        harness.assertNotOnBattlefield(player1, "Mudflat Village");
        harness.assertInGraveyard(player1, "Mudflat Village");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInHand(player1, target.getName());
        harness.assertNotInGraveyard(player1, target.getName());
    }

    private void addVillage() {
        harness.addToBattlefield(player1, new MudflatVillage());
    }
}
