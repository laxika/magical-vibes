package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MutagenManLivingOoze.class, GrizzlyBears.class})
class MutagenManLivingOozeTest extends BaseCardTest {

    @Test
    void createsXMutagenTokens() {
        castMutagenMan(2);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList())
                .hasSize(2);
    }

    @Test
    void reducesMutagenActivationCostAndPutsCounterOnTargetCreature() {
        castMutagenMan(1);
        Permanent mutagen = findPermanent(player1, "Mutagen");
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        mutagen.untap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mutagen), 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mutagen);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void cannotActivateMutagenOutsideSorcerySpeed() {
        castMutagenMan(1);
        Permanent mutagen = findPermanent(player1, "Mutagen");
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        mutagen.untap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mutagen), 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery");
    }

    private void castMutagenMan(int xValue) {
        harness.setHand(player1, List.of(new MutagenManLivingOoze()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.castArtifact(player1, 0, xValue);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
