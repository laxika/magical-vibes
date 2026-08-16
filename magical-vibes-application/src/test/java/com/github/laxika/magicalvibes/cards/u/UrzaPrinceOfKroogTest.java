package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UrzaPrinceOfKroogTest extends BaseCardTest {

    @Test
    @DisplayName("Artifact creatures you control get +2/+2")
    void boostsControlledArtifactCreatures() {
        harness.addToBattlefield(player1, new UrzaPrinceOfKroog());
        Permanent ownArtifactCreature = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent opposingArtifactCreature = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        assertThat(gqs.getEffectivePower(gd, ownArtifactCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownArtifactCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposingArtifactCreature)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, opposingArtifactCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creates a 1/1 Soldier creature token copy of a controlled artifact")
    void createsArtifactTokenCopy() {
        Permanent urza = harness.addToBattlefieldAndReturn(player1, new UrzaPrinceOfKroog());
        urza.setSummoningSick(false);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new HowlingMine());
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo("Howling Mine");
        assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SOLDIER);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target an artifact controlled by an opponent")
    void cannotTargetOpponentsArtifact() {
        Permanent urza = harness.addToBattlefieldAndReturn(player1, new UrzaPrinceOfKroog());
        urza.setSummoningSick(false);
        Permanent opposingArtifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opposingArtifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
