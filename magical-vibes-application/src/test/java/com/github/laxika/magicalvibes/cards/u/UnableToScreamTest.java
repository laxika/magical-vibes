package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.m.MasterOfPearls;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnableToScream.class, AirElemental.class, FountainOfYouth.class, MasterOfPearls.class})
class UnableToScreamTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature becomes a 0/2 Toy artifact and loses its abilities")
    void transformsEnchantedCreature() {
        Permanent airElemental = addCreatureReady(player2, new AirElemental());
        addAttachedAura(airElemental);

        assertThat(gqs.getEffectivePower(gd, airElemental)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, airElemental)).isEqualTo(2);
        assertThat(gqs.isArtifact(gd, airElemental)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, airElemental)).contains(CardSubtype.TOY);
        assertThat(gqs.hasKeyword(gd, airElemental,
                com.github.laxika.magicalvibes.model.Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A face-down enchanted creature cannot be turned face up")
    void preventsFaceDownCreatureFromTurningFaceUp() {
        Permanent faceDown = addFaceDownMasterOfPearls();
        addAttachedAura(faceDown);
        prepareToTurnFaceUp();

        assertThatThrownBy(() -> harness.turnFaceUp(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(faceDown)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be turned face up");
        assertThat(faceDown.isFaceDown()).isTrue();
    }

    @Test
    @DisplayName("Removing Unable to Scream lets the face-down creature turn face up")
    void removingAuraAllowsFaceUpTurn() {
        Permanent faceDown = addFaceDownMasterOfPearls();
        Permanent aura = addAttachedAura(faceDown);
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        prepareToTurnFaceUp();

        harness.turnFaceUp(player2, gd.playerBattlefields.get(player2.getId()).indexOf(faceDown));

        assertThat(faceDown.isFaceDown()).isFalse();
    }

    @Test
    @DisplayName("Unable to Scream can target only a creature")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new UnableToScream()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addFaceDownMasterOfPearls() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new MasterOfPearls());
        permanent.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        return permanent;
    }

    private Permanent addAttachedAura(Permanent target) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new UnableToScream());
        aura.setAttachedTo(target.getId());
        return aura;
    }

    private void prepareToTurnFaceUp() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.WHITE, 2);
    }
}
