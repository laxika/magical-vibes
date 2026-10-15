package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FearlessPup;
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
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PortOfKarfell.class, GrizzlyBears.class, HolyDay.class, LightningBolt.class,
        Opt.class, Shock.class, FearlessPup.class})
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

    @Test
    @DisplayName("Pays costs before resolution and can return a newly milled creature")
    void returnsNewlyMilledCreature() {
        Permanent port = harness.addToBattlefieldAndReturn(player1, new PortOfKarfell());
        Card creature = new FearlessPup();
        Card remaining = new PortOfKarfell();
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(new PortOfKarfell(), creature,
                new PortOfKarfell(), new PortOfKarfell(), remaining));
        addActivationMana();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(port);
        harness.assertInGraveyard(player1, "Port of Karfell");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        harness.handleGraveyardCardChosen(player1,
                gd.playerGraveyards.get(player1.getId()).indexOf(creature));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement()
                .matches(p -> p.getCard().getId().equals(creature.getId()) && p.isTapped());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Mills all available cards from a short library and still returns a creature")
    void returnsCreatureAfterMillingShortLibrary() {
        harness.addToBattlefield(player1, new PortOfKarfell());
        Card creature = new FearlessPup();
        Card opposingCreature = new FearlessPup();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(opposingCreature));
        harness.setLibrary(player1, List.of(creature));
        addActivationMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.handleGraveyardCardChosen(player1,
                gd.playerGraveyards.get(player1.getId()).indexOf(creature));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement()
                .matches(p -> p.getCard().getId().equals(creature.getId()) && p.isTapped());
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingCreature);
    }

    @Test
    @DisplayName("With an empty library the return is mandatory and cannot choose a noncreature")
    void returnsExistingCreatureWithEmptyLibrary() {
        harness.addToBattlefield(player1, new PortOfKarfell());
        Card creature = new FearlessPup();
        Card noncreature = new PortOfKarfell();
        harness.setGraveyard(player1, List.of(creature, noncreature));
        harness.setLibrary(player1, List.of());
        addActivationMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1,
                gd.playerGraveyards.get(player1.getId()).indexOf(noncreature)))
                .isInstanceOf(IllegalStateException.class);

        harness.handleGraveyardCardChosen(player1,
                gd.playerGraveyards.get(player1.getId()).indexOf(creature));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement()
                .matches(p -> p.getCard().getId().equals(creature.getId()) && p.isTapped());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(noncreature).doesNotContain(creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
    }
}
