package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NishobaBrawler;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EerieSoultender.class, Forest.class, NishobaBrawler.class})
class EerieSoultenderTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield and mills three cards")
    void millsThreeCardsOnEnter() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.castFromHand(player1, new EerieSoultender(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Exiles itself and returns another creature to hand")
    void exilesItselfAndReturnsAnotherCreatureToHand() {
        Card soultender = new EerieSoultender();
        Card creature = new NishobaBrawler();
        harness.setGraveyard(player1, List.of(soultender, creature));
        addActivationMana();

        harness.activateGraveyardAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(creature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(soultender.getId()));
    }

    @Test
    @DisplayName("Cannot target itself")
    void cannotTargetItself() {
        Card soultender = new EerieSoultender();
        harness.setGraveyard(player1, List.of(soultender));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(soultender.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(soultender);
        assertThat(gd.exiledCards).noneMatch(exiled -> exiled.card().getId().equals(soultender.getId()));
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    void millsOnlyAvailableCardsFromShortLibrary() {
        Card first = new Forest();
        Card second = new NishobaBrawler();
        harness.setLibrary(player1, List.of(first, second));
        harness.castFromHand(player1, new EerieSoultender(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void cannotTargetNoncreatureCard() {
        Card soultender = new EerieSoultender();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(soultender, land));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(soultender, land);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void cannotTargetCreatureInOpponentsGraveyard() {
        Card soultender = new EerieSoultender();
        Card creature = new NishobaBrawler();
        harness.setGraveyard(player1, List.of(soultender));
        harness.setGraveyard(player2, List.of(creature));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(soultender);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void canReturnAnotherCopyAndExilesSourceBeforeResolution() {
        Card source = new EerieSoultender();
        Card target = new EerieSoultender();
        harness.setGraveyard(player1, List.of(source, target));
        addActivationMana();

        harness.activateGraveyardAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(target);
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(source.getId()));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotReturnTargetThatLeftGraveyardAndDoesNotRefundExileCost() {
        Card source = new EerieSoultender();
        Card target = new NishobaBrawler();
        harness.setGraveyard(player1, List.of(source, target));
        addActivationMana();
        harness.activateGraveyardAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(target));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(source, target);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(source.getId()));
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(target.getId()));
    }
}
