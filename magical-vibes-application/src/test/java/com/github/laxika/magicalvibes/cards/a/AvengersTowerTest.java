package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(AvengersTower.class)
class AvengersTowerTest extends BaseCardTest {

    @Test
    void tapsForColorlessMana() {
        addReadyTower();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void tapsForManaRestrictedToHeroSpellsAndAbilities() {
        addReadyTower();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.RED)).isZero();
        assertThat(pool.getSubtypeSpellOrAbilityManaForColor(Set.of(CardSubtype.HERO), ManaColor.RED))
                .isEqualTo(1);
    }

    @Test
    void HeroRestrictedManaCanCastHeroSpell() {
        addReadyTower();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");

        Card hero = createCreature("Test Hero", "{R}", CardSubtype.HERO);
        harness.setHand(player1, List.of(hero));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void HeroRestrictedManaCannotCastNonHeroSpell() {
        addReadyTower();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");

        Card creature = createCreature("Test Creature", "{R}");
        harness.setHand(player1, List.of(creature));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void looksAtTopThreeAndPutsChosenHeroIntoHand() {
        addReadyTower();
        Card hero = createCreature("Test Hero", "{G}", CardSubtype.HERO);
        Card instant = createNonCreature("Test Instant");
        Card artifact = createNonCreature("Test Artifact");
        Card belowTopThree = createNonCreature("Below Top Three");
        setLibrary(hero, instant, artifact, belowTopThree);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(hero);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(hero);
        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(instant, artifact);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(instant, artifact, belowTopThree);
    }

    @Test
    void mayDeclineHeroAndPutsAllThreeCardsOnBottom() {
        addReadyTower();
        Card hero = createCreature("Test Hero", "{G}", CardSubtype.HERO);
        Card instant = createNonCreature("Test Instant");
        Card artifact = createNonCreature("Test Artifact");
        setLibrary(hero, instant, artifact);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(hero, instant, artifact);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 1, 0)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(hero);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(hero, instant, artifact);
    }

    private Permanent addReadyTower() {
        Permanent tower = harness.addToBattlefieldAndReturn(player1, new AvengersTower());
        tower.setSummoningSick(false);
        return tower;
    }

    private Card createCreature(String name, String manaCost, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost(manaCost);
        card.setColor(CardColor.RED);
        card.setSubtypes(List.of(subtypes));
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private Card createNonCreature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost("{R}");
        card.setColor(CardColor.RED);
        return card;
    }

    private void setLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }
}
