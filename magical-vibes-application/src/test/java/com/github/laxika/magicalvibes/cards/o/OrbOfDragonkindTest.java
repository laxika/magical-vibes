package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({OrbOfDragonkind.class, ShivanDragon.class, GrizzlyBears.class})
class OrbOfDragonkindTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping the Orb adds two mana in independently chosen colors for Dragons")
    void addsTwoDragonRestrictedManaInAnyCombination() {
        addReadyOrb();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "BLUE");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getSubtypeSpellOrAbilityManaForColor(Set.of(CardSubtype.DRAGON), ManaColor.RED))
                .isEqualTo(1);
        assertThat(pool.getSubtypeSpellOrAbilityManaForColor(Set.of(CardSubtype.DRAGON), ManaColor.BLUE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Dragon-restricted mana pays for a Dragon ability")
    void restrictedManaPaysForDragonAbility() {
        Permanent orb = addReadyOrb();
        Permanent dragon = addCreatureReady(player1, new ShivanDragon());
        int powerBefore = gqs.getEffectivePower(gd, dragon);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "RED");

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(powerBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(orb);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getSubtypeSpellOrAbilityManaForColor(Set.of(CardSubtype.DRAGON), ManaColor.RED))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing the Orb reveals only Dragon cards among the top seven")
    void revealsDragonFromTopSeven() {
        ShivanDragon dragon = new ShivanDragon();
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), dragon, new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        addReadyOrb();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).hasSize(7);
        assertThat(choice.validCardIds()).containsExactly(dragon.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(dragon.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(dragon);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6).doesNotContain(dragon);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof OrbOfDragonkind);
    }

    @Test
    @DisplayName("Declining the Dragon reveal bottoms all seven cards")
    void decliningRevealBottomsAllCards() {
        ShivanDragon dragon = new ShivanDragon();
        harness.setLibrary(player1, List.of(
                dragon, new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        addReadyOrb();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(dragon);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(7);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void manaAbilityRequiresOneMana() {
        Permanent orb = addReadyOrb();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(orb.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void manaAbilityConsumesItsGenericPaymentAndResolvesWithoutTheStack() {
        Permanent orb = addReadyOrb();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "RED");

        assertThat(orb.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
    }

    @Test
    void restrictedManaPaysForDragonSpell() {
        ShivanDragon dragon = new ShivanDragon();
        harness.setHand(player1, List.of(dragon));
        addReadyOrb();
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "RED");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == dragon);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void restrictedManaCannotPayForNonDragonSpell() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addReadyOrb();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.handleListChoice(player1, "GREEN");

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void restrictedManaCannotPayForAnotherOrbsSacrificeAbility() {
        addReadyOrb();
        Permanent otherOrb = addReadyOrb();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "RED");

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(otherOrb);
        assertThat(otherOrb.isTapped()).isFalse();
    }

    @Test
    void sacrificeIsPaidBeforeResolutionAndShortLibraryStillAllowsDeclining() {
        ShivanDragon dragon = new ShivanDragon();
        GrizzlyBears bear = new GrizzlyBears();
        harness.setLibrary(player1, List.of(dragon, bear));
        Permanent orb = addReadyOrb();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(orb);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(orb.getCard());
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(dragon);

        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(dragon, bear);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(dragon, bear);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void dragonBelowTopSevenIsNotEligibleAndUnseenCardsStayOnTop() {
        ShivanDragon eighthCard = new ShivanDragon();
        List<GrizzlyBears> topSeven = List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player1, List.of(topSeven.get(0), topSeven.get(1), topSeven.get(2),
                topSeven.get(3), topSeven.get(4), topSeven.get(5), topSeven.get(6), eighthCard));
        addReadyOrb();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(eighthCard);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(8);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(eighthCard);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 8))
                .containsExactlyInAnyOrderElementsOf(topSeven);
    }

    @Test
    void emptyLibraryResolvesWithoutAChoice() {
        harness.setLibrary(player1, List.of());
        Permanent orb = addReadyOrb();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(orb.getCard());
    }

    private Permanent addReadyOrb() {
        return addCreatureReady(player1, new OrbOfDragonkind());
    }
}
