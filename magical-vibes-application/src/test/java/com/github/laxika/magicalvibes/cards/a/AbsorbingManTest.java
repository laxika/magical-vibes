package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AbsorbingMan.class, Forest.class, GloriousAnthem.class, GrizzlyBears.class, Pacifism.class, SolRing.class})
class AbsorbingManTest extends BaseCardTest {

    @Test
    @DisplayName("First main phase trigger targets artifacts, non-Aura enchantments, and lands")
    void targetSelectionUsesPrintedRestrictions() {
        Permanent absorbingMan = addAbsorbingMan();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = new Permanent(new Pacifism());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        beginFirstMainPhase();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(artifact.getId(), enchantment.getId(), land.getId())
                .doesNotContain(absorbingMan.getId(), creature.getId(), aura.getId());
    }

    @Test
    @DisplayName("Copying an artifact applies Absorbing Man's copy exceptions")
    void copiesArtifactWithExceptions() {
        Permanent absorbingMan = addAbsorbingMan();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SolRing());

        resolveCopy(artifact);

        assertThat(absorbingMan.getCard().getName()).isEqualTo("Absorbing Man");
        assertThat(absorbingMan.getCard().getPower()).isEqualTo(4);
        assertThat(absorbingMan.getCard().getToughness()).isEqualTo(4);
        assertThat(absorbingMan.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(absorbingMan.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(absorbingMan.getCard().getSubtypes()).contains(CardSubtype.HUMAN, CardSubtype.VILLAIN);
        assertThat(absorbingMan.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(absorbingMan.getCard().getKeywords()).contains(Keyword.VIGILANCE);
    }

    @Test
    @DisplayName("Copy lasts through cleanup and reverts at the controller's next turn")
    void copyRevertsAtControllersNextTurn() {
        Permanent absorbingMan = addAbsorbingMan();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        resolveCopy(land);
        assertThat(absorbingMan.getCard().getName()).isEqualTo("Absorbing Man");
        assertThat(absorbingMan.getCard().hasType(CardType.LAND)).isTrue();
        assertThat(absorbingMan.getCard().hasType(CardType.CREATURE)).isTrue();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(absorbingMan.getCard().getName()).isEqualTo("Absorbing Man");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(absorbingMan.getCard().getName()).isEqualTo("Absorbing Man");
        assertThat(absorbingMan.getCard().getPower()).isEqualTo(4);
        assertThat(absorbingMan.getCard().getToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The optional trigger does nothing when there is no legal target")
    void optionalTriggerCanBeDeclined() {
        Permanent absorbingMan = addAbsorbingMan();
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        beginFirstMainPhase();
        harness.passBothPriorities();

        assertThat(absorbingMan.getCard().getName()).isEqualTo("Absorbing Man");
        assertThat(absorbingMan.getCard().getPower()).isEqualTo(4);
        assertThat(absorbingMan.getCard().getToughness()).isEqualTo(4);
    }

    private Permanent addAbsorbingMan() {
        return harness.addToBattlefieldAndReturn(player1, new AbsorbingMan());
    }

    private void beginFirstMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void resolveCopy(Permanent target) {
        beginFirstMainPhase();
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
    }
}
