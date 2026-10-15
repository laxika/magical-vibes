package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.o.OtherworldlyGaze;
import com.github.laxika.magicalvibes.cards.s.SqueeTheImmortal;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RootcoilCreeper.class, GrizzlyBears.class, SqueeTheImmortal.class, RollingTemblor.class, HolyDay.class,
        OtherworldlyGaze.class})
class RootcoilCreeperTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability adds one mana of the chosen color")
    void addsAnyColorMana() {
        addCreatureReady(player1, new RootcoilCreeper());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second ability adds two mana of one color restricted to graveyard spells")
    void addsGraveyardOnlyMana() {
        addCreatureReady(player1, new RootcoilCreeper());
        SqueeTheImmortal squee = new SqueeTheImmortal();
        harness.setGraveyard(player1, List.of(squee));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).getGraveyardOnlyMana(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof SqueeTheImmortal);
        assertThat(gd.playerManaPools.get(player1.getId()).getGraveyardOnlyMana(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Graveyard-only mana cannot pay for a spell cast from hand")
    void graveyardOnlyManaCannotPayForSpellFromHand() {
        addCreatureReady(player1, new RootcoilCreeper());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The third ability returns a flashback card from exile and exiles Rootcoil Creeper")
    void returnsFlashbackCardFromExile() {
        Permanent rootcoil = addCreatureReady(player1, new RootcoilCreeper());
        Card flashbackCard = new RollingTemblor();
        harness.setExile(player1, List.of(flashbackCard));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 2, null, flashbackCard.getId(), Zone.EXILE);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(rootcoil);
        assertThat(gd.playerHands.get(player1.getId())).contains(flashbackCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(flashbackCard);
    }

    @Test
    @DisplayName("The third ability rejects an exiled card without flashback")
    void rejectsExiledCardWithoutFlashback() {
        addCreatureReady(player1, new RootcoilCreeper());
        Card cardWithoutFlashback = new HolyDay();
        harness.setExile(player1, List.of(cardWithoutFlashback));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 2, null, cardWithoutFlashback.getId(), Zone.EXILE))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void graveyardManaPaysBothColoredAndGenericFlashbackCosts() {
        Permanent rootcoil = addCreatureReady(player1, new RootcoilCreeper());
        Card flashbackCard = new OtherworldlyGaze();
        harness.setGraveyard(player1, List.of(flashbackCard));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(rootcoil.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.castFlashback(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(flashbackCard);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getGraveyardOnlyMana(ManaColor.BLUE)).isZero();
    }

    @Test
    void rejectsFlashbackCardOwnedByOpponent() {
        Permanent rootcoil = addCreatureReady(player1, new RootcoilCreeper());
        Card flashbackCard = new OtherworldlyGaze();
        harness.setExile(player2, List.of(flashbackCard));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 2, null, flashbackCard.getId(), Zone.EXILE))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(rootcoil);
        assertThat(rootcoil.isTapped()).isFalse();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(flashbackCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void exileAndManaCostsArePaidBeforeTheReturnResolves() {
        Permanent rootcoil = addCreatureReady(player1, new RootcoilCreeper());
        Card flashbackCard = new OtherworldlyGaze();
        harness.setExile(player1, List.of(flashbackCard));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 2, null, flashbackCard.getId(), Zone.EXILE);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(rootcoil);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(rootcoil.getCard(), flashbackCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(flashbackCard);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(flashbackCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(rootcoil.getCard()).doesNotContain(flashbackCard);
    }

    @Test
    void graveyardOnlyManaCannotPayForTheReturnAbility() {
        addCreatureReady(player1, new RootcoilCreeper());
        addCreatureReady(player1, new RootcoilCreeper());
        Card flashbackCard = new OtherworldlyGaze();
        harness.setExile(player1, List.of(flashbackCard));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 1, 2, null, flashbackCard.getId(), Zone.EXILE))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(flashbackCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void firstManaAbilityTapsAndResolvesWithoutUsingTheStack() {
        Permanent rootcoil = addCreatureReady(player1, new RootcoilCreeper());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(rootcoil.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void summoningSicknessPreventsEachTapAbility(int abilityIndex) {
        Permanent rootcoil = addCreatureReady(player1, new RootcoilCreeper());
        rootcoil.setSummoningSick(true);
        Card flashbackCard = new OtherworldlyGaze();
        harness.setExile(player1, List.of(flashbackCard));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null,
                abilityIndex == 2 ? flashbackCard.getId() : null,
                abilityIndex == 2 ? Zone.EXILE : null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(rootcoil.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(rootcoil);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returnAbilityDoesNothingIfTargetLeavesExile() {
        Permanent rootcoil = addCreatureReady(player1, new RootcoilCreeper());
        Card flashbackCard = new OtherworldlyGaze();
        harness.setExile(player1, List.of(flashbackCard));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 2, null, flashbackCard.getId(), Zone.EXILE);

        gd.removeFromExile(flashbackCard.getId());
        harness.setGraveyard(player1, List.of(flashbackCard));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(flashbackCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(flashbackCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(rootcoil.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(rootcoil);
        assertThat(gd.stack).isEmpty();
    }
}
