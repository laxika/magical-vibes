package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MachineGodsEffigy.class, GrizzlyBears.class})
class MachineGodsEffigyTest extends BaseCardTest {

    @Test
    void copiesCreatureAsNoncreatureArtifactAndAddsBlueManaAbility() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        MachineGodsEffigy effigy = new MachineGodsEffigy();
        harness.setHand(player1, List.of(effigy));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());

        Permanent copy = findEffigy(effigy);
        assertThat(copy.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(copy.getCard().getPower()).isEqualTo(2);
        assertThat(copy.getCard().getToughness()).isEqualTo(2);
        assertThat(copy.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(copy.getCard().hasType(CardType.CREATURE)).isFalse();

        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(copy));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void decliningToCopyLeavesArtifactThatCanProduceBlueMana() {
        MachineGodsEffigy effigy = new MachineGodsEffigy();
        harness.setHand(player1, List.of(effigy));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent entered = findEffigy(effigy);
        assertThat(entered.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(entered.getCard().hasType(CardType.CREATURE)).isFalse();

        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(entered));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    private Permanent findEffigy(MachineGodsEffigy effigy) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(effigy.getId()))
                .findFirst()
                .orElseThrow();
    }
}
