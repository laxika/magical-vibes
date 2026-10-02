package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BrassGnat;
import com.github.laxika.magicalvibes.cards.p.PrismaticLens;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AcademyRuins.class, BrassGnat.class, PrismaticLens.class, AshcoatBear.class})
class AcademyRuinsTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one colorless mana")
    void tapsForColorless() {
        Permanent ruins = addReadyRuins();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(ruins.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Puts a target artifact card from the graveyard on top of the library")
    void putsTargetArtifactOnTopOfLibrary() {
        Permanent ruins = addReadyRuins();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        Card artifact = new BrassGnat();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setLibrary(player1, List.of(new AshcoatBear()));

        int ruinsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(ruins);
        harness.activateAbility(player1, ruinsIndex, 1, null, artifact.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(artifact);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(artifact);
    }

    @Test
    @DisplayName("Only artifact cards in your graveyard are legal targets")
    void rejectsNonArtifactTarget() {
        Permanent ruins = addReadyRuins();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        Card nonArtifact = new AshcoatBear();
        harness.setGraveyard(player1, List.of(nonArtifact));

        int ruinsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(ruins);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, ruinsIndex, 1, null, nonArtifact.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an artifact in the opponent's graveyard")
    void rejectsOpponentsArtifact() {
        addReadyRuins();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        Card artifact = new PrismaticLens();
        harness.setGraveyard(player2, List.of(artifact));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 1, null, artifact.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(artifact);
    }

    @Test
    @DisplayName("Recovery pays one generic and one blue mana and taps before resolution")
    void paysCostsAndUsesStack() {
        Permanent ruins = addReadyRuins();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        Card artifact = new PrismaticLens();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, artifact.getId(), Zone.GRAVEYARD);

        assertThat(ruins.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Recovery requires blue mana even when enough generic mana is available")
    void rejectsMissingBlueMana() {
        Permanent ruins = addReadyRuins();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Card artifact = new PrismaticLens();
        harness.setGraveyard(player1, List.of(artifact));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 1, null, artifact.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ruins.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(artifact);
    }

    @Test
    @DisplayName("A tapped Academy Ruins cannot activate either ability")
    void rejectsActivationsWhileTapped() {
        Permanent ruins = addReadyRuins();
        ruins.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        Card artifact = new PrismaticLens();
        harness.setGraveyard(player1, List.of(artifact));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 1, null, artifact.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Recovery does nothing if its target leaves the graveyard before resolution")
    void targetLeavesGraveyard() {
        addReadyRuins();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        Card artifact = new PrismaticLens();
        Card otherArtifact = new PrismaticLens();
        Card libraryCard = new AshcoatBear();
        harness.setGraveyard(player1, List.of(artifact, otherArtifact));
        harness.setLibrary(player1, List.of(libraryCard));

        harness.activateAbility(player1, 0, 1, null, artifact.getId(), Zone.GRAVEYARD);
        harness.setGraveyard(player1, List.of(otherArtifact));
        harness.setExile(player1, List.of(artifact));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherArtifact);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Recovery still resolves if Academy Ruins leaves the battlefield")
    void recoveryIndependentOfSource() {
        Permanent ruins = addReadyRuins();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        Card artifact = new PrismaticLens();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, artifact.getId(), Zone.GRAVEYARD);
        gd.playerBattlefields.get(player1.getId()).remove(ruins);
        harness.setGraveyard(player1, List.of(artifact, ruins.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ruins.getCard());
    }

    @Test
    @DisplayName("A newly controlled noncreature land can tap for mana immediately")
    void landCanTapImmediately() {
        Permanent ruins = harness.addToBattlefieldAndReturn(player1, new AcademyRuins());
        ruins.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(ruins.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Recovery also requires the generic portion of its mana cost")
    void rejectsMissingGenericMana() {
        Permanent ruins = addReadyRuins();
        harness.addMana(player1, ManaColor.BLUE, 1);
        Card artifact = new PrismaticLens();
        harness.setGraveyard(player1, List.of(artifact));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 1, null, artifact.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ruins.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(artifact);
    }

    private Permanent addReadyRuins() {
        return addCreatureReady(player1, new AcademyRuins());
    }
}
