package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FlamewakePhoenix;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.cards.s.ShockmawDragon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrucibleOfTheSpiritDragon.class, ShockmawDragon.class, FlamewakePhoenix.class, ShivanDragon.class})
class CrucibleOfTheSpiritDragonTest extends BaseCardTest {

    @Test
    @DisplayName("First ability adds one colorless mana")
    void tapsForColorlessMana() {
        Permanent land = addReadyLand();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(manaPool().get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Second ability puts a storage counter on the land")
    void paysToPutStorageCounter() {
        Permanent land = addReadyLand();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.STORAGE)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Third ability removes X counters and adds Dragon-restricted mana in chosen colors")
    void removesXCountersForDragonManaInAnyCombination() {
        Permanent land = addReadyLand();
        land.setCounterCount(CounterType.STORAGE, 3);

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class).maxValue()).isEqualTo(3);
        harness.handleXValueChosen(player1, 2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.RED.name());
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        ManaPool pool = manaPool();
        assertThat(land.getCounterCount(CounterType.STORAGE)).isEqualTo(1);
        assertThat(pool.get(ManaColor.RED)).isZero();
        assertThat(pool.get(ManaColor.BLUE)).isZero();
        assertThat(pool.getSubtypeSpellOrAbilityManaForColor(Set.of(CardSubtype.DRAGON), ManaColor.RED)).isEqualTo(1);
        assertThat(pool.getSubtypeSpellOrAbilityManaForColor(Set.of(CardSubtype.DRAGON), ManaColor.BLUE)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing zero storage counters produces no mana")
    void removingZeroCountersProducesNoMana() {
        Permanent land = addReadyLand();
        land.setCounterCount(CounterType.STORAGE, 2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.handleXValueChosen(player1, 0);

        assertThat(land.getCounterCount(CounterType.STORAGE)).isEqualTo(2);
        assertThat(manaPool().getTotal()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Dragon-restricted mana cannot cast a non-Dragon spell")
    void restrictedManaCannotCastNonDragonSpell() {
        Permanent land = addReadyLand();
        land.setCounterCount(CounterType.STORAGE, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.handleXValueChosen(player1, 1);
        harness.handleListChoice(player1, ManaColor.RED.name());

        harness.setHand(player1, List.of(createCreature("Test Goblin", CardSubtype.GOBLIN)));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Dragon-restricted mana can cast a Dragon spell")
    void restrictedManaCanCastDragonSpell() {
        Permanent land = addReadyLand();
        land.setCounterCount(CounterType.STORAGE, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.handleXValueChosen(player1, 1);
        harness.handleListChoice(player1, ManaColor.RED.name());

        harness.setHand(player1, List.of(createCreature("Test Dragon", CardSubtype.DRAGON)));
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(manaPool().getSubtypeSpellOrAbilityManaForColor(Set.of(CardSubtype.DRAGON), ManaColor.RED)).isZero();
    }

    @Test
    void storageCounterIsAddedOnlyWhenAbilityResolves() {
        Permanent land = addReadyLand();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(land.isTapped()).isTrue();
        assertThat(manaPool().getTotal()).isZero();
        assertThat(land.getCounterCount(CounterType.STORAGE)).isZero();
        harness.passBothPriorities();
        assertThat(land.getCounterCount(CounterType.STORAGE)).isEqualTo(1);
    }

    @Test
    void restrictedManaPaysColoredAndGenericCostsOfRealDragonSpell() {
        Permanent land = addReadyLand();
        land.setCounterCount(CounterType.STORAGE, 6);
        harness.setHand(player1, List.of(new ShockmawDragon()));

        harness.activateAbility(player1, 0, 2, 6, null);
        harness.handleListChoice(player1, ManaColor.RED.name());
        harness.handleListChoice(player1, ManaColor.RED.name());
        for (int i = 0; i < 4; i++) {
            harness.handleListChoice(player1, ManaColor.BLUE.name());
        }

        assertThat(gd.stack).isEmpty();
        assertThat(land.getCounterCount(CounterType.STORAGE)).isZero();
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        assertThat(manaPool().getTotal()).isZero();
        assertThat(manaPool().getSubtypeSpellOrAbilityManaForColor(Set.of(CardSubtype.DRAGON), ManaColor.RED)).isZero();
        assertThat(manaPool().getSubtypeSpellOrAbilityManaForColor(Set.of(CardSubtype.DRAGON), ManaColor.BLUE)).isZero();
    }

    @Test
    void restrictedManaCannotPayColoredCostOfRealNonDragonSpell() {
        Permanent land = addReadyLand();
        land.setCounterCount(CounterType.STORAGE, 1);
        harness.setHand(player1, List.of(new FlamewakePhoenix()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 2, 1, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(manaPool().getSubtypeSpellOrAbilityManaForColor(Set.of(CardSubtype.DRAGON), ManaColor.RED))
                .isEqualTo(1);
    }

    @Test
    void restrictedManaCannotPayGenericCostOfNonDragonSpell() {
        Permanent land = addReadyLand();
        land.setCounterCount(CounterType.STORAGE, 1);
        harness.setHand(player1, List.of(new FlamewakePhoenix()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 2, 1, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(manaPool().getSubtypeSpellOrAbilityManaForColor(Set.of(CardSubtype.DRAGON), ManaColor.BLUE))
                .isEqualTo(1);
    }

    @Test
    void restrictedManaCanPayForDragonActivatedAbility() {
        Permanent land = addReadyLand();
        land.setCounterCount(CounterType.STORAGE, 1);
        Permanent dragon = addCreatureReady(player1, new ShivanDragon());

        harness.activateAbility(player1, 0, 2, 1, null);
        harness.handleListChoice(player1, ManaColor.RED.name());
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dragon.getPowerModifier()).isEqualTo(1);
        assertThat(manaPool().getTotal()).isZero();
        assertThat(manaPool().getSubtypeSpellOrAbilityManaForColor(Set.of(CardSubtype.DRAGON), ManaColor.RED)).isZero();
    }

    @Test
    void restrictedManaCannotPayForNonDragonStorageAbility() {
        Permanent firstLand = addReadyLand();
        firstLand.setCounterCount(CounterType.STORAGE, 1);
        Permanent secondLand = addReadyLand();

        harness.activateAbility(player1, 0, 2, 1, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(secondLand.isTapped()).isFalse();
        assertThat(secondLand.getCounterCount(CounterType.STORAGE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotRemoveMoreCountersThanPresent() {
        Permanent land = addReadyLand();
        land.setCounterCount(CounterType.STORAGE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, 3, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(land.isTapped()).isFalse();
        assertThat(land.getCounterCount(CounterType.STORAGE)).isEqualTo(2);
        assertThat(manaPool().getTotal()).isZero();
    }

    @Test
    void zeroCountersStillAllowsZeroManaActivation() {
        Permanent land = addReadyLand();

        harness.activateAbility(player1, 0, 2, 0, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(manaPool().getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addReadyLand() {
        return addCreatureReady(player1, new CrucibleOfTheSpiritDragon());
    }

    private ManaPool manaPool() {
        return gd.playerManaPools.get(player1.getId());
    }

    private static Card createCreature(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{R}");
        card.setColor(CardColor.RED);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(subtype));
        return card;
    }
}
