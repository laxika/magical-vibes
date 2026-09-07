package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DragonWhelp;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
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

@CardUsed({HavenOfTheSpiritDragon.class, DragonWhelp.class, LlanowarElves.class, UginTheSpiritDragon.class})
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

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new DragonWhelp()));
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Dragon-restricted mana cannot cast a non-Dragon creature spell")
    void restrictedManaCannotCastNonDragonCreatureSpell() {
        addReadyHaven();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");
        harness.setHand(player1, List.of(new LlanowarElves()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Third ability returns a Dragon from the graveyard and sacrifices the land")
    void returnsDragonFromGraveyard() {
        Permanent haven = addReadyHaven();
        Card dragon = new DragonWhelp();
        harness.setGraveyard(player1, List.of(dragon));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null, dragon.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Dragon Whelp");
        harness.assertNotInGraveyard(player1, "Dragon Whelp");
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
        Card elves = new LlanowarElves();
        harness.setGraveyard(player1, List.of(elves));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 2, null, elves.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyHaven() {
        Permanent haven = new Permanent(new HavenOfTheSpiritDragon());
        haven.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(haven);
        return haven;
    }
}
