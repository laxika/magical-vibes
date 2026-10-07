package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.r.RaffinesInformant;
import com.github.laxika.magicalvibes.cards.g.Goldhound;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnlicensedHearse.class, RaffinesInformant.class, Goldhound.class})
class UnlicensedHearseTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles up to two cards from one graveyard and uses them for its power and toughness")
    void exilesCardsAndGetsTheirCountAsPowerAndToughness() {
        Permanent hearse = addReadyHearse(player1);
        Card card1 = new RaffinesInformant();
        Card card2 = new Goldhound();
        Card card3 = new RaffinesInformant();
        harness.setGraveyard(player2, new ArrayList<>(List.of(card1, card2, card3)));

        harness.activateAbilityWithGraveyardTargets(player1, hearseIndex(hearse), 0,
                List.of(card1.getId(), card2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId).containsExactly(card3.getId());
        assertThat(gd.getCardsExiledByPermanent(hearse.getId()))
                .extracting(Card::getId).containsExactly(card1.getId(), card2.getId());
        assertThat(gqs.getEffectivePower(gd, hearse)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hearse)).isEqualTo(2);
    }

    @Test
    @DisplayName("Targets must all come from a single graveyard")
    void targetsMustShareOneGraveyard() {
        Permanent hearse = addReadyHearse(player1);
        Card mine = new RaffinesInformant();
        Card theirs = new Goldhound();
        harness.setGraveyard(player1, List.of(mine));
        harness.setGraveyard(player2, List.of(theirs));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, hearseIndex(hearse), 0,
                List.of(mine.getId(), theirs.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single graveyard");
    }

    @Test
    @DisplayName("Crew 2 animates the Hearse and taps the crew")
    void crewAnimatesHearse() {
        Permanent hearse = addReadyHearse(player1);
        Card fuel = new Goldhound();
        harness.setGraveyard(player2, List.of(fuel));
        harness.activateAbilityWithGraveyardTargets(player1, hearseIndex(hearse), 0, List.of(fuel.getId()));
        harness.passBothPriorities();
        Permanent crew = addCreatureReady(player1, new RaffinesInformant());

        harness.activateAbility(player1, hearseIndex(hearse), 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, hearse)).isTrue();
        assertThat(crew.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hearse);
        assertThat(gqs.getEffectivePower(gd, hearse)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hearse)).isEqualTo(1);
    }

    @Test
    void canActivateWithZeroTargetsInEmptyGraveyards() {
        Permanent hearse = addReadyHearse(player1);
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());

        harness.activateAbilityWithGraveyardTargets(player1, hearseIndex(hearse), 0, List.of());
        assertThat(hearse.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(hearse.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hearse);
    }

    @Test
    void canChooseZeroTargetsEvenWhenCardsAreAvailable() {
        Permanent hearse = addReadyHearse(player1);
        Card fuel = new Goldhound();
        harness.setGraveyard(player1, List.of(fuel));

        harness.activateAbilityWithGraveyardTargets(player1, hearseIndex(hearse), 0, List.of());
        harness.passBothPriorities();

        assertThat(hearse.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(fuel);
        assertThat(gd.getCardsExiledByPermanent(hearse.getId())).isEmpty();
    }

    @Test
    void repeatedActivationsAccumulateCardsFromEitherGraveyard() {
        Permanent hearse = addReadyHearse(player1);
        Card mine = new Goldhound();
        Card theirs = new RaffinesInformant();
        harness.setGraveyard(player1, List.of(mine));
        harness.setGraveyard(player2, List.of(theirs));

        harness.activateAbilityWithGraveyardTargets(player1, hearseIndex(hearse), 0, List.of(mine.getId()));
        harness.passBothPriorities();
        harness.performUntapStep(player1);
        harness.activateAbilityWithGraveyardTargets(player1, hearseIndex(hearse), 0, List.of(theirs.getId()));
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(hearse.getId())).containsExactly(mine, theirs);
        assertThat(gqs.getEffectivePower(gd, hearse)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hearse)).isEqualTo(2);
    }

    @Test
    void remainingLegalTargetIsExiledWhenOtherTargetIsRemovedInResponse() {
        Permanent hearse = addReadyHearse(player1);
        Permanent opposingHearse = addReadyHearse(player2);
        Card first = new Goldhound();
        Card second = new RaffinesInformant();
        harness.setGraveyard(player2, new ArrayList<>(List.of(first, second)));

        harness.activateAbilityWithGraveyardTargets(player1, hearseIndex(hearse), 0,
                List.of(first.getId(), second.getId()));
        harness.activateAbilityWithGraveyardTargets(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(opposingHearse), 0, List.of(first.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(hearse.getId())).containsExactly(second);
        assertThat(gd.getCardsExiledByPermanent(opposingHearse.getId())).containsExactly(first);
        assertThat(gqs.getEffectivePower(gd, hearse)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hearse)).isEqualTo(1);
    }

    @Test
    void crewWithoutExiledCardsPutsHearseIntoGraveyard() {
        Permanent hearse = addReadyHearse(player1);
        Permanent crew = addCreatureReady(player1, new RaffinesInformant());

        harness.activateAbility(player1, hearseIndex(hearse), 1, null, null);
        harness.passBothPriorities();

        assertThat(crew.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(hearse);
        harness.assertInGraveyard(player1, "Unlicensed Hearse");
    }

    @Test
    void cannotChooseMoreThanTwoOrDuplicateTargets() {
        Permanent hearse = addReadyHearse(player1);
        Card first = new Goldhound();
        Card second = new RaffinesInformant();
        Card third = new Goldhound();
        harness.setGraveyard(player1, List.of(first, second, third));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, hearseIndex(hearse), 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, hearseIndex(hearse), 0,
                List.of(first.getId(), first.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(hearse.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second, third);
    }

    private int hearseIndex(Permanent hearse) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(hearse);
    }

    private Permanent addReadyHearse(Player player) {
        return addCreatureReady(player, new UnlicensedHearse());
    }
}
