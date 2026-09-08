package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChampionOfStraySoulsTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices X other creatures and returns X targeted creature cards")
    void sacrificesOtherCreaturesAndReturnsTargets() {
        Permanent champion = addReadyChampion();
        Permanent firstSacrifice = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondSacrifice = addCreatureReady(player1, new GrizzlyBears());
        Card firstTarget = new GrizzlyBears();
        Card secondTarget = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstTarget, secondTarget));
        addManaForFirstAbility();

        gs.activateAbility(gd, player1, indexOf(champion), 0, 2, null, Zone.GRAVEYARD,
                List.of(firstTarget.getId(), secondTarget.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(firstSacrifice, secondSacrifice);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(firstTarget.getId(), secondTarget.getId());
    }

    @Test
    @DisplayName("The source creature cannot be paid as one of the other creatures")
    void sourceCannotBeSacrificedAsOtherCreature() {
        Permanent champion = addReadyChampion();
        addCreatureReady(player1, new GrizzlyBears());
        addManaForFirstAbility();

        assertThatThrownBy(() -> gs.activateAbility(gd, player1, indexOf(champion), 0, 2, null,
                Zone.GRAVEYARD, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(champion);
    }

    @Test
    @DisplayName("The graveyard ability puts this card on top of its owner's library")
    void returnsItselfToTopOfLibrary() {
        Card champion = new ChampionOfStraySouls();
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(champion));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateGraveyardAbility(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(champion);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(champion);
    }

    private Permanent addReadyChampion() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        return addCreatureReady(player1, new ChampionOfStraySouls());
    }

    private void addManaForFirstAbility() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
