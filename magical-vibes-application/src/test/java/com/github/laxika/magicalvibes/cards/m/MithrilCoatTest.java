package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KamahlPitFighter;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({MithrilCoat.class, KamahlPitFighter.class, GrizzlyBears.class, Naturalize.class})
class MithrilCoatTest extends BaseCardTest {

    @Test
    @DisplayName("Mithril Coat attaches to a legendary creature only after its entry trigger resolves")
    void entersAttachedToLegendaryCreature() {
        Permanent kamahl = addCreatureReady(player1, new KamahlPitFighter());
        harness.setHand(player1, List.of(new MithrilCoat()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent coat = findPermanent(player1, "Mithril Coat");
        assertThat(coat.getAttachedTo()).isNull();
        assertThat(gqs.hasKeyword(gd, kamahl, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.handlePermanentChosen(player1, kamahl.getId());
        resolveAllTriggers();

        assertThat(coat.getAttachedTo()).isEqualTo(kamahl.getId());
        assertThat(gqs.hasKeyword(gd, kamahl, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Mithril Coat cannot target a nonlegendary creature on entry")
    void cannotTargetNonlegendaryCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent kamahl = addCreatureReady(player1, new KamahlPitFighter());
        harness.setHand(player1, List.of(new MithrilCoat()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, kamahl.getId());
        resolveAllTriggers();
        assertThat(findPermanent(player1, "Mithril Coat").getAttachedTo()).isEqualTo(kamahl.getId());
    }

    @Test
    @DisplayName("Equip can move Mithril Coat to a nonlegendary creature you control")
    void equipCanTargetAnyCreatureYouControl() {
        Permanent kamahl = addCreatureReady(player1, new KamahlPitFighter());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent coat = harness.addToBattlefieldAndReturn(player1, new MithrilCoat());
        coat.setAttachedTo(kamahl.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 2, null, bears.getId());
        harness.passBothPriorities();

        assertThat(coat.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.hasKeyword(gd, kamahl, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void canBeCastWithoutCreatures() {
        harness.setHand(player1, List.of(new MithrilCoat()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Mithril Coat").getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void remainsUnattachedWhenOnlyNonlegendaryCreaturesAreAvailable() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MithrilCoat()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Mithril Coat").getAttachedTo()).isNull();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void entryTriggerCannotTargetOpponentsLegendaryCreature() {
        Permanent ownCreature = addCreatureReady(player1, new KamahlPitFighter());
        Permanent opponentCreature = addCreatureReady(player2, new KamahlPitFighter());
        harness.setHand(player1, List.of(new MithrilCoat()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, ownCreature.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Mithril Coat").getAttachedTo()).isEqualTo(ownCreature.getId());
    }

    @Test
    void flashAllowsCastingAndAttachingDuringOpponentsUpkeep() {
        Permanent kamahl = addCreatureReady(player1, new KamahlPitFighter());
        harness.setHand(player1, List.of(new MithrilCoat()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, kamahl.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Mithril Coat").getAttachedTo()).isEqualTo(kamahl.getId());
        assertThat(gqs.hasKeyword(gd, kamahl, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void equippedCreatureSurvivesLethalDamage() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent coat = harness.addToBattlefieldAndReturn(player1, new MithrilCoat());
        coat.setAttachedTo(bears.getId());
        addCreatureReady(player2, new KamahlPitFighter());

        harness.activateAbility(player2, 0, null, bears.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(coat.getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    void coatItselfCannotBeDestroyed() {
        Permanent coat = harness.addToBattlefieldAndReturn(player1, new MithrilCoat());
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, coat.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Mithril Coat")).isSameAs(coat);
    }

    @Test
    void equipRemainsSorcerySpeedDespiteFlash() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new MithrilCoat());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Mithril Coat").getAttachedTo()).isNull();
    }

    @Test
    void equipmentStaysUnattachedIfTargetDiesInResponseToEntryTrigger() {
        Permanent kamahl = addCreatureReady(player1, new KamahlPitFighter());
        addCreatureReady(player2, new KamahlPitFighter());
        harness.setHand(player1, List.of(new MithrilCoat()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, kamahl.getId());
        harness.activateAbility(player2, 0, null, kamahl.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Kamahl, Pit Fighter");
        assertThat(findPermanent(player1, "Mithril Coat").getAttachedTo()).isNull();
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new MithrilCoat());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Mithril Coat").getAttachedTo()).isNull();
    }

    @Test
    void equipRequiresThreeMana() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new MithrilCoat());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Mithril Coat").getAttachedTo()).isNull();
    }
}
