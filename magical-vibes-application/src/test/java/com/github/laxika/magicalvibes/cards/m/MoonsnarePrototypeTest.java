package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoonsnarePrototype.class, GrizzlyBears.class, Island.class, Spellbook.class})
class MoonsnarePrototypeTest extends BaseCardTest {

    @Test
    @DisplayName("Taps an untapped artifact to produce colorless mana")
    void tapsArtifactToProduceColorlessMana() {
        Permanent prototype = harness.addToBattlefieldAndReturn(player1, new MoonsnarePrototype());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(prototype.isTapped()).isTrue();
        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taps an untapped creature to produce colorless mana")
    void tapsCreatureToProduceColorlessMana() {
        Permanent prototype = harness.addToBattlefieldAndReturn(player1, new MoonsnarePrototype());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(prototype.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Channel lets the target's owner put the permanent on top")
    void channelPutsTargetOnTop() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card libraryCard = new Island();
        harness.setLibrary(player2, List.of(libraryCard));
        prepareChannel();

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Top");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard(), libraryCard);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Moonsnare Prototype");
    }

    @Test
    @DisplayName("Channel lets the target's owner put the permanent on the bottom")
    void channelPutsTargetOnBottom() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card libraryCard = new Island();
        harness.setLibrary(player2, List.of(libraryCard));
        prepareChannel();

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard, target.getCard());
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Moonsnare Prototype");
    }

    @Test
    @DisplayName("Channel cannot target a land")
    void channelCannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        prepareChannel();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareChannel() {
        harness.setHand(player1, List.of(new MoonsnarePrototype()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
