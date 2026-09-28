package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SynthEradicator.class, GrizzlyBears.class})
class SynthEradicatorTest extends BaseCardTest {

    @Test
    void attackingMayGetTwoEnergyInsteadOfPlayPermission() {
        Card topCard = libraryCard("Top card");
        harness.setLibrary(player1, List.of(topCard));
        addReadySynth();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId)
                .containsExactly(topCard.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
    }

    @Test
    void decliningEnergyGrantsPlayPermissionForExiledCard() {
        Card topCard = libraryCard("Top card");
        harness.setLibrary(player1, List.of(topCard));
        addReadySynth();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
    }

    @Test
    void activatedAbilityPaysEnergyTapsAndDealsDamageToAnyTarget() {
        Permanent synth = addReadySynth();
        gd.playerEnergyCounters.put(player1.getId(), 3);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(synth.isTapped()).isTrue();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        harness.assertLife(player2, 17);
    }

    @Test
    void activatedAbilityRequiresThreeEnergy() {
        addReadySynth();
        gd.playerEnergyCounters.put(player1.getId(), 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("three energy counters");
    }

    private Permanent addReadySynth() {
        Permanent synth = new Permanent(new SynthEradicator());
        synth.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(synth);
        return synth;
    }

    private Card libraryCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }
}
