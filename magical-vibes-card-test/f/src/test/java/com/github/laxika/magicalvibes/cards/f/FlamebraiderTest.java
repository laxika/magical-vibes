package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.k.KulrathZealot;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({Flamebraider.class, FlameChainMauler.class, FormidableSpeaker.class, KulrathZealot.class})
class FlamebraiderTest extends BaseCardTest {

    private static Card createCreature(String name, String manaCost, CardColor color, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(subtypes));
        return card;
    }

    @Test
    @DisplayName("Tapping Flamebraider adds two independently chosen Elemental-restricted mana")
    void tappingAddsTwoRestrictedMana() {
        Permanent flamebraider = addCreatureReady(player1, new Flamebraider());

        harness.activateAbility(player1, 0, null, null);

        assertThat(flamebraider.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "BLUE");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.RED)).isZero();
        assertThat(pool.get(ManaColor.BLUE)).isZero();
        assertThat(pool.getSubtypeSpellOrAbilityManaForColor(Set.of(CardSubtype.ELEMENTAL), ManaColor.RED)).isEqualTo(1);
        assertThat(pool.getSubtypeSpellOrAbilityManaForColor(Set.of(CardSubtype.ELEMENTAL), ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Flamebraider mana can pay for an Elemental spell")
    void manaCanCastElementalSpell() {
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSubtypeSpellOrAbilityMana(CardSubtype.ELEMENTAL, ManaColor.RED, 1);

        harness.setHand(player1, List.of(createCreature("Test Elemental", "{R}", CardColor.RED, CardSubtype.ELEMENTAL)));
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Flamebraider mana cannot pay for a non-Elemental spell")
    void manaCannotCastNonElementalSpell() {
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSubtypeSpellOrAbilityMana(CardSubtype.ELEMENTAL, ManaColor.RED, 1);

        harness.setHand(player1, List.of(createCreature("Test Goblin", "{R}", CardColor.RED, CardSubtype.GOBLIN)));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void producedManaPaysColoredAndGenericCostsOfElementalSpell() {
        addCreatureReady(player1, new Flamebraider());
        harness.setHand(player1, List.of(new FlameChainMauler()));
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "BLUE");

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Flame-Chain Mauler");
        assertThat(gd.playerManaPools.get(player1.getId()).getSubtypeSpellOrAbilityManaTotal(
                Set.of(CardSubtype.ELEMENTAL))).isZero();
    }

    @Test
    void producedManaPaysForElementalPermanentAbility() {
        addCreatureReady(player1, new Flamebraider());
        Permanent mauler = harness.addToBattlefieldAndReturn(player1, new FlameChainMauler());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "RED");

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mauler)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getSubtypeSpellOrAbilityManaTotal(
                Set.of(CardSubtype.ELEMENTAL))).isZero();
    }

    @Test
    void producedManaCannotPayForNonElementalAbility() {
        Permanent flamebraider = addCreatureReady(player1, new Flamebraider());
        Permanent speaker = addCreatureReady(player1, new FormidableSpeaker());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.handleListChoice(player1, "WHITE");

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, flamebraider.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(speaker.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getSubtypeSpellOrAbilityManaTotal(
                Set.of(CardSubtype.ELEMENTAL))).isEqualTo(2);
    }

    @Test
    void producedManaCanPayForElementalBasicLandcyclingFromHand() {
        addCreatureReady(player1, new Flamebraider());
        KulrathZealot zealot = new KulrathZealot();
        harness.setHand(player1, List.of(zealot));
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "BLACK");

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(zealot);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(zealot);
        assertThat(gd.playerManaPools.get(player1.getId()).getSubtypeSpellOrAbilityManaTotal(
                Set.of(CardSubtype.ELEMENTAL))).isZero();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent flamebraider = harness.addToBattlefieldAndReturn(player1, new Flamebraider());
        flamebraider.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(flamebraider.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getSubtypeSpellOrAbilityManaTotal(
                Set.of(CardSubtype.ELEMENTAL))).isZero();
    }

    @Test
    void cannotActivateAgainWhileTapped() {
        addCreatureReady(player1, new Flamebraider());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "WHITE");
        harness.handleListChoice(player1, "BLACK");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getSubtypeSpellOrAbilityManaTotal(
                Set.of(CardSubtype.ELEMENTAL))).isEqualTo(2);
    }
}
