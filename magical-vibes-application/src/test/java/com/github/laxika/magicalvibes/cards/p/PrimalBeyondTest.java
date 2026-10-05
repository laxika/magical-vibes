package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BrighthearthBanneret;
import com.github.laxika.magicalvibes.cards.f.FrogtosserBanneret;
import com.github.laxika.magicalvibes.cards.m.MoongloveChangeling;
import com.github.laxika.magicalvibes.cards.s.Smokebraider;
import com.github.laxika.magicalvibes.cards.s.SunflareShaman;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrimalBeyond.class, Forest.class, Smokebraider.class, BrighthearthBanneret.class,
        FrogtosserBanneret.class, MoongloveChangeling.class, SunflareShaman.class})
class PrimalBeyondTest extends BaseCardTest {

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
    @DisplayName("Enters tapped when you have no Elemental card in hand")
    void entersTappedWithoutElemental() {
        harness.setHand(player1, List.of(new PrimalBeyond(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        assertThat(findLand(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Revealing an Elemental lets it enter untapped")
    void entersUntappedWhenRevealing() {
        harness.setHand(player1, List.of(new PrimalBeyond(), new Smokebraider()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining to reveal makes it enter tapped even with an Elemental in hand")
    void entersTappedWhenDeclining() {
        harness.setHand(player1, List.of(new PrimalBeyond(), new Smokebraider()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findLand(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("First ability taps for one colorless mana")
    void tappingProducesColorless() {
        addLandReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findLand(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Second ability adds one mana of a chosen color, Elemental-restricted")
    void tappingProducesRestrictedAnyColorMana() {
        Permanent land = addLandReady(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.BLUE)).isEqualTo(0);
        assertThat(pool.getSubtypeSpellOrAbilityManaForColor(Set.of(CardSubtype.ELEMENTAL), ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Restricted mana can cast an Elemental spell but not a non-Elemental spell")
    void restrictedManaOnlyForElementals() {
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSubtypeSpellOrAbilityMana(CardSubtype.ELEMENTAL, ManaColor.RED, 1);

        Card goblin = createCreature("Test Goblin", "{R}", CardColor.RED, CardSubtype.GOBLIN);
        harness.setHand(player1, List.of(goblin));
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        Card elemental = createCreature("Test Elemental", "{R}", CardColor.RED, CardSubtype.ELEMENTAL);
        harness.setHand(player1, List.of(elemental));
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Test Elemental");
    }

    @Test
    void changelingCanBeRevealedAndStaysInHand() {
        harness.setHand(player1, List.of(new PrimalBeyond(), new MoongloveChangeling()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand(player1).isTapped()).isFalse();
        harness.assertInHand(player1, "Moonglove Changeling");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void restrictedManaPaysColoredCostOfElementalSpell() {
        addLandReady(player1);
        harness.setHand(player1, List.of(new BrighthearthBanneret()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Brighthearth Banneret");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void restrictedManaCannotPayGenericCostOfNonElementalSpell() {
        addLandReady(player1);
        harness.setHand(player1, List.of(new FrogtosserBanneret()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Frogtosser Banneret");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    void colorlessManaCanPayForNonElementalSpell() {
        addLandReady(player1);
        harness.setHand(player1, List.of(new FrogtosserBanneret()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Frogtosser Banneret");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void restrictedManaCanCastChangelingSpell() {
        addLandReady(player1);
        harness.setHand(player1, List.of(new MoongloveChangeling()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLACK");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Moonglove Changeling");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void restrictedManaCanActivateElementalOnBattlefield() {
        addLandReady(player1);
        Permanent shaman = addCreatureReady(player1, new SunflareShaman());
        harness.setGraveyard(player1, List.of(new BrighthearthBanneret()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");
        harness.activateAbility(player1, 1, 0, null, player2.getId());

        assertThat(shaman.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
    }

    @Test
    void restrictedManaCanActivateChangelingOnBattlefield() {
        addLandReady(player1);
        Permanent changeling = harness.addToBattlefieldAndReturn(player1, new MoongloveChangeling());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLACK");
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, changeling, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void restrictedManaCannotPayForElementalReinforceFromHand() {
        addLandReady(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BrighthearthBanneret());
        harness.setHand(player1, List.of(new BrighthearthBanneret()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Brighthearth Banneret");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    private Permanent addLandReady(Player player) {
        return addCreatureReady(player, new PrimalBeyond());
    }

    private Permanent findLand(Player player) {
        return findPermanent(player, "Primal Beyond");
    }
}
