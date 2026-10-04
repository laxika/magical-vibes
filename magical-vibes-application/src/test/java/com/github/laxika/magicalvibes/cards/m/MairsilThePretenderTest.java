package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MairsilThePretender.class, RodOfRuin.class})
class MairsilThePretenderTest extends BaseCardTest {

    @Test
    void cagesCardFromHandAndGrantsItsActivatedAbilityOncePerTurn() {
        MairsilThePretender mairsil = new MairsilThePretender();
        RodOfRuin rodOfRuin = new RodOfRuin();
        harness.setHand(player1, List.of(mairsil, rodOfRuin));
        addMairsilMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiZoneExileChoice.class)).isNotNull();

        harness.handleMultipleCardsChosen(player1, List.of(rodOfRuin.getId()));

        assertThat(gd.exiledCardsWithCageCounters).contains(rodOfRuin.getId());
        assertThat(gd.findExiledCard(rodOfRuin.getId()).sourcePermanentId()).isNull();

        Permanent mairsilPermanent = findMairsil(mairsil);
        mairsilPermanent.setSummoningSick(false);
        int mairsilIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mairsilPermanent);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, mairsilIndex, 0, null, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);

        mairsilPermanent.untap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() -> harness.activateAbility(player1, mairsilIndex, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cagesAnArtifactOrCreatureCardFromTheGraveyard() {
        MairsilThePretender mairsil = new MairsilThePretender();
        RodOfRuin rodOfRuin = new RodOfRuin();
        harness.setHand(player1, List.of(mairsil));
        harness.setGraveyard(player1, List.of(rodOfRuin));
        addMairsilMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(rodOfRuin.getId()));

        assertThat(gd.exiledCardsWithCageCounters).contains(rodOfRuin.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void addMairsilMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private Permanent findMairsil(MairsilThePretender mairsil) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(mairsil.getId()))
                .findFirst()
                .orElseThrow();
    }
}
