package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JunkJet.class, GrizzlyBears.class, Spellbook.class})
class JunkJetTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a Junk token")
    void entersWithJunkToken() {
        harness.setHand(player1, List.of(new JunkJet()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent junk = findPermanent(player1, "Junk");
        assertThat(junk.getCard().getSubtypes()).contains(CardSubtype.JUNK);
        assertThat(junk.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing another artifact doubles the equipped creature's power")
    void sacrificesArtifactAndDoublesEquippedCreaturePower() {
        Permanent creature = addReady(new GrizzlyBears());
        Permanent jet = addReady(new JunkJet());
        jet.setAttachedTo(creature.getId());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, battlefieldIndex(jet), 0, null, null);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Spellbook");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot sacrifice Junk Jet itself for its activated ability")
    void requiresAnotherArtifact() {
        Permanent jet = addReady(new JunkJet());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(jet), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Equip ability attaches Junk Jet to a creature you control")
    void equipsToControlledCreature() {
        Permanent jet = addReady(new JunkJet());
        Permanent creature = addReady(new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(jet), 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(jet.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addReady(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
