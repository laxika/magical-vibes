package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SurgicalMetamorph.class, Island.class})
class SurgicalMetamorphTest extends BaseCardTest {

    @Test
    void nonStartingPlayerPaysOneLessAndCopiesAnyPermanentAsAnArtifact() {
        gd.activePlayerId = player2.getId();
        harness.addToBattlefield(player1, new Island());
        SurgicalMetamorph source = new SurgicalMetamorph();
        harness.setHand(player2, List.of(source));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        UUID islandId = harness.getPermanentId(player1, "Island");
        harness.handlePermanentChosen(player2, islandId);

        Permanent copy = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(source.getId()))
                .findFirst().orElseThrow();
        assertThat(copy.getCard().getName()).isEqualTo("Island");
        assertThat(copy.getCard().hasType(CardType.LAND)).isTrue();
        assertThat(copy.getCard().hasType(CardType.ARTIFACT)).isTrue();
    }

    @Test
    void startingPlayerDoesNotGetTheCostReduction() {
        harness.setHand(player1, List.of(new SurgicalMetamorph()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void decliningToCopyPutsTheZeroToughnessCreatureInTheGraveyard() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new SurgicalMetamorph()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Surgical Metamorph");
        harness.assertInGraveyard(player1, "Surgical Metamorph");
        harness.assertOnBattlefield(player1, "Island");
    }

    @Test
    void emptyBattlefieldLeavesNoCopyAndTheCreatureDies() {
        harness.setHand(player1, List.of(new SurgicalMetamorph()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Surgical Metamorph");
        harness.assertInGraveyard(player1, "Surgical Metamorph");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void copyingOwnTappedIslandEntersUntappedAndCanImmediatelyProduceMana() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new Island());
        original.tap();
        harness.setHand(player1, List.of(new SurgicalMetamorph()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, original.getId());

        Permanent copy = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThat(copy.isTapped()).isFalse();
        assertThat(copy.getCard().hasType(CardType.LAND)).isTrue();
        assertThat(copy.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(copy.getCard().hasType(CardType.CREATURE)).isFalse();
        harness.tapPermanent(player1, 1);
        assertThat(copy.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void nonStartingPlayerStillNeedsBlueMana() {
        gd.activePlayerId = player2.getId();
        harness.setHand(player2, List.of(new SurgicalMetamorph()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castArtifact(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
