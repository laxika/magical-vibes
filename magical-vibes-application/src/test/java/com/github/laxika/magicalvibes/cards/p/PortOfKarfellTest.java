package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PortOfKarfellTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new PortOfKarfell()));

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement()
                .matches(Permanent::isTapped);
    }

    @Test
    @DisplayName("Tapping adds one blue mana")
    void tapsForBlueMana() {
        Permanent port = harness.addToBattlefieldAndReturn(player1, new PortOfKarfell());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(port.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing it mills four cards and returns a chosen creature tapped")
    void millsAndReturnsCreatureTapped() {
        Permanent port = harness.addToBattlefieldAndReturn(player1, new PortOfKarfell());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(new HolyDay(), new LightningBolt(), new Opt(), new Shock()));
        addActivationMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Port of Karfell", "Holy Day", "Lightning Bolt", "Opt", "Shock");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);

        harness.handleGraveyardCardChosen(player1,
                gd.playerGraveyards.get(player1.getId()).indexOf(creature));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getId().equals(creature.getId()))
                .singleElement()
                .matches(Permanent::isTapped);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(port);
    }

    @Test
    @DisplayName("Does not prompt when the graveyard has no creature card")
    void doesNothingWithoutCreatureCard() {
        harness.addToBattlefield(player1, new PortOfKarfell());
        harness.setGraveyard(player1, List.of(new HolyDay()));
        harness.setLibrary(player1, List.of(new HolyDay(), new LightningBolt(), new Opt(), new Shock()));
        addActivationMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Port of Karfell");
        harness.assertInGraveyard(player1, "Holy Day");
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
    }
}
