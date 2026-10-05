package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GoldenTailDisciple;
import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({MoonsnarePrototype.class, GoldenTailDisciple.class, Island.class})
class MoonsnarePrototypeTest extends BaseCardTest {

    @Test
    @DisplayName("Taps an untapped artifact to produce colorless mana")
    void tapsArtifactToProduceColorlessMana() {
        Permanent prototype = harness.addToBattlefieldAndReturn(player1, new MoonsnarePrototype());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MoonsnarePrototype());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(prototype.isTapped()).isTrue();
        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taps an untapped creature to produce colorless mana")
    void tapsCreatureToProduceColorlessMana() {
        Permanent prototype = harness.addToBattlefieldAndReturn(player1, new MoonsnarePrototype());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoldenTailDisciple());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(prototype.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Channel lets the target's owner put the permanent on top")
    void channelPutsTargetOnTop() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoldenTailDisciple());
        Card libraryCard = new Island();
        harness.setLibrary(player2, List.of(libraryCard));
        prepareChannel();

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Top");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard(), libraryCard);
        harness.assertNotOnBattlefield(player2, "Golden-Tail Disciple");
        harness.assertInGraveyard(player1, "Moonsnare Prototype");
    }

    @Test
    @DisplayName("Channel lets the target's owner put the permanent on the bottom")
    void channelPutsTargetOnBottom() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoldenTailDisciple());
        Card libraryCard = new Island();
        harness.setLibrary(player2, List.of(libraryCard));
        prepareChannel();

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard, target.getCard());
        harness.assertNotOnBattlefield(player2, "Golden-Tail Disciple");
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

    @Test
    @DisplayName("Cannot pay both tap costs using only the Prototype itself")
    void cannotTapPrototypeForBothCosts() {
        Permanent prototype = harness.addToBattlefieldAndReturn(player1, new MoonsnarePrototype());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(prototype.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Cannot tap an opponent's permanent to pay the mana ability cost")
    void cannotTapOpponentsPermanent() {
        harness.addToBattlefield(player1, new MoonsnarePrototype());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GoldenTailDisciple());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot use an already tapped creature to pay the mana ability cost")
    void cannotTapAlreadyTappedCreature() {
        harness.addToBattlefield(player1, new MoonsnarePrototype());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoldenTailDisciple());
        creature.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Channel discards the card and pays mana before resolving")
    void channelPaysCostsBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoldenTailDisciple());
        prepareChannel();

        harness.activateHandAbility(player1, 0, target.getId());

        harness.assertNotInHand(player1, "Moonsnare Prototype");
        harness.assertInGraveyard(player1, "Moonsnare Prototype");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);

        harness.passBothPriorities();
        harness.handleListChoice(player2, "Top");
        assertThat(gd.playerDecks.get(player2.getId())).first().isEqualTo(target.getCard());
    }

    @Test
    @DisplayName("Channel can target an artifact controlled by its activator")
    void channelCanTargetOwnArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MoonsnarePrototype());
        Card libraryCard = new Island();
        harness.setLibrary(player1, List.of(libraryCard));
        prepareChannel();

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Bottom");

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard, target.getCard());
        harness.assertNotOnBattlefield(player1, "Moonsnare Prototype");
    }

    @Test
    @DisplayName("Channel requires the full five mana activation cost")
    void channelCannotActivateWithInsufficientMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoldenTailDisciple());
        harness.setHand(player1, List.of(new MoonsnarePrototype()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Moonsnare Prototype");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    private void prepareChannel() {
        harness.setHand(player1, List.of(new MoonsnarePrototype()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
