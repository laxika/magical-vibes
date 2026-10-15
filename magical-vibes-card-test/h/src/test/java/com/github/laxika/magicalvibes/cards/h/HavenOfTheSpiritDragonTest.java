package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.s.StormwingDragon;
import com.github.laxika.magicalvibes.cards.c.ColossodonYearling;
import com.github.laxika.magicalvibes.cards.c.CribSwap;
import com.github.laxika.magicalvibes.cards.u.UginTheSpiritDragon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HavenOfTheSpiritDragon.class, StormwingDragon.class, ColossodonYearling.class, CribSwap.class, UginTheSpiritDragon.class})
class HavenOfTheSpiritDragonTest extends BaseCardTest {

    @Test
    @DisplayName("First ability adds one colorless mana")
    void addsColorlessMana() {
        Permanent haven = addReadyHaven();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(haven.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability adds mana restricted to Dragon creature spells")
    void addsDragonCreatureSpellMana() {
        addReadyHaven();

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.RED)).isZero();
        assertThat(pool.getSubtypeCreatureManaForColor(Set.of(CardSubtype.DRAGON), ManaColor.RED))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Dragon-restricted mana can cast a Dragon creature spell")
    void restrictedManaCastsDragonCreatureSpell() {
        addReadyHaven();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");

        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.setHand(player1, List.of(new StormwingDragon()));
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Dragon-restricted mana cannot cast a non-Dragon creature spell")
    void restrictedManaCannotCastNonDragonCreatureSpell() {
        addReadyHaven();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of(new ColossodonYearling()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Third ability returns a Dragon from the graveyard and sacrifices the land")
    void returnsDragonFromGraveyard() {
        addReadyHaven();
        Card dragon = new StormwingDragon();
        harness.setGraveyard(player1, List.of(dragon));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null, dragon.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Stormwing Dragon");
        harness.assertNotInGraveyard(player1, "Stormwing Dragon");
        harness.assertNotOnBattlefield(player1, "Haven of the Spirit Dragon");
    }

    @Test
    @DisplayName("Third ability returns a Ugin planeswalker from the graveyard")
    void returnsUginFromGraveyard() {
        addReadyHaven();
        Card ugin = new UginTheSpiritDragon();
        harness.setGraveyard(player1, List.of(ugin));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null, ugin.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Ugin, the Spirit Dragon");
        harness.assertNotInGraveyard(player1, "Ugin, the Spirit Dragon");
    }

    @Test
    @DisplayName("Third ability cannot target an ineligible graveyard card")
    void rejectsIneligibleGraveyardCard() {
        addReadyHaven();
        Card beast = new ColossodonYearling();
        harness.setGraveyard(player1, List.of(beast));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 2, null, beast.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Third ability cannot return a noncreature changeling card")
    void rejectsNoncreatureDragonCard() {
        addReadyHaven();
        Card card = new CribSwap();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 2, null, card.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Third ability cannot target a Dragon in an opponent's graveyard")
    void rejectsOpponentsDragon() {
        addReadyHaven();
        Card dragon = new StormwingDragon();
        harness.setGraveyard(player2, List.of(dragon));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 2, null, dragon.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The land is sacrificed and mana is paid before its return ability resolves")
    void paysCostsBeforeResolution() {
        addReadyHaven();
        Card dragon = new StormwingDragon();
        harness.setGraveyard(player1, List.of(dragon));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null, dragon.getId(), Zone.GRAVEYARD);

        harness.assertNotOnBattlefield(player1, "Haven of the Spirit Dragon");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(dragon)
                .anyMatch(card -> card instanceof HavenOfTheSpiritDragon);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A target that leaves the graveyard is not returned")
    void doesNotReturnRemovedTarget() {
        addReadyHaven();
        Card dragon = new StormwingDragon();
        harness.setGraveyard(player1, List.of(dragon));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 2, null, dragon.getId(), Zone.GRAVEYARD);
        gd.playerGraveyards.get(player1.getId()).remove(dragon);
        harness.setExile(player1, List.of(dragon));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(dragon);
        assertThat(gd.findExiledCard(dragon.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Dragon-restricted mana can pay a Dragon spell's generic cost")
    void restrictedManaPaysGenericDragonCost() {
        addReadyHaven();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new StormwingDragon()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getSubtypeCreatureManaForColor(Set.of(CardSubtype.DRAGON), ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Dragon-restricted mana cannot pay the land's return ability cost")
    void restrictedManaCannotPayAbilityCost() {
        Permanent haven = addReadyHaven();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");
        haven.untap();
        Card dragon = new StormwingDragon();
        harness.setGraveyard(player1, List.of(dragon));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 2, null, dragon.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Haven of the Spirit Dragon");
    }
    private Permanent addReadyHaven() {
        return harness.addToBattlefieldAndReturn(player1, new HavenOfTheSpiritDragon());
    }
}
