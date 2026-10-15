package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({MeriaScholarOfAntiquity.class, Ornithopter.class, Forest.class})
class MeriaScholarOfAntiquityTest extends BaseCardTest {

    @Test
    @DisplayName("Taps a nontoken artifact to add green mana")
    void tapsNontokenArtifactForGreenMana() {
        Permanent meria = addMeria();
        Permanent artifact = addArtifact();

        harness.activateAbility(player1, indexOf(meria), 0, null, null);

        assertThat(artifact.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Exiles the top card after tapping two nontoken artifacts")
    void exilesTopCardAndAllowsItToBePlayedThisTurn() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        Permanent meria = addMeria();
        Permanent artifact1 = addArtifact();
        Permanent artifact2 = addArtifact();

        harness.activateAbility(player1, indexOf(meria), 1, null, null);
        resolveAllTriggers();

        assertThat(artifact1.isTapped()).isTrue();
        assertThat(artifact2.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).as("library after resolving Meria's ability").isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
    }

    @Test
    @DisplayName("Cannot tap a token artifact for either ability")
    void cannotUseTokenArtifact() {
        Permanent meria = addMeria();
        Card tokenCard = new Ornithopter().createRuntimeCopy();
        tokenCard.setToken(true);
        Permanent tokenArtifact = harness.addToBattlefieldAndReturn(player1, tokenCard);
        tokenArtifact.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(meria), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(meria), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(tokenArtifact.isTapped()).isFalse();
    }

    @Test
    void canTapSummoningSickArtifactWhileMeriaIsSummoningSickAndTapped() {
        Permanent meria = harness.addToBattlefieldAndReturn(player1, new MeriaScholarOfAntiquity());
        meria.setSummoningSick(true);
        meria.tap();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        artifact.setSummoningSick(true);

        harness.activateAbility(player1, indexOf(meria), 0, null, null);

        assertThat(artifact.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotReuseTappedArtifact() {
        Permanent meria = addMeria();
        Permanent artifact = addArtifact();
        artifact.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(meria), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotUseOpponentsArtifactOrNonartifactPermanent() {
        Permanent meria = addMeria();
        Permanent opponentArtifact = addCreatureReady(player2, new Ornithopter());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(meria), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(meria), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(opponentArtifact.isTapped()).isFalse();
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    void cannotPaySecondAbilityWithOnlyOneUntappedArtifact() {
        Permanent meria = addMeria();
        Permanent artifact = addArtifact();
        Permanent tappedArtifact = addArtifact();
        tappedArtifact.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(meria), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(artifact.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void secondAbilityUsesStackAndCanTapSummoningSickArtifacts() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        Permanent meria = addMeria();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        first.setSummoningSick(true);
        second.setSummoningSick(true);

        harness.activateAbility(player1, indexOf(meria), 1, null, null);

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        resolveAllTriggers();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    @Test
    void emptyLibraryStillAllowsPayingTheCost() {
        harness.setLibrary(player1, List.of());
        Permanent meria = addMeria();
        Permanent first = addArtifact();
        Permanent second = addArtifact();

        harness.activateAbility(player1, indexOf(meria), 1, null, null);
        resolveAllTriggers();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canActuallyPlayExiledLand() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        Permanent meria = addMeria();
        addArtifact();
        addArtifact();

        harness.activateAbility(player1, indexOf(meria), 1, null, null);
        resolveAllTriggers();
        harness.castFromExile(player1, topCard.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(topCard.getId()));
    }

    @Test
    void exiledSpellRequiresItsNormalManaCost() {
        MeriaScholarOfAntiquity topCard = new MeriaScholarOfAntiquity();
        harness.setLibrary(player1, List.of(topCard));
        Permanent meria = addMeria();
        addArtifact();
        addArtifact();

        harness.activateAbility(player1, indexOf(meria), 1, null, null);
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, topCard.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void exiledCreatureStillRequiresSorceryTiming() {
        MeriaScholarOfAntiquity topCard = new MeriaScholarOfAntiquity();
        harness.setLibrary(player1, List.of(topCard));
        Permanent meria = addMeria();
        addArtifact();
        addArtifact();
        harness.activateAbility(player1, indexOf(meria), 1, null, null);
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void exiledLandDoesNotGrantAnAdditionalLandPlay() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        Permanent meria = addMeria();
        addArtifact();
        addArtifact();
        harness.activateAbility(player1, indexOf(meria), 1, null, null);
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    @Test
    void unplayedCardRemainsExiledButPermissionExpiresAfterTheTurn() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        Permanent meria = addMeria();
        addArtifact();
        addArtifact();
        harness.activateAbility(player1, indexOf(meria), 1, null, null);
        resolveAllTriggers();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(topCard.getId());
    }

    private Permanent addMeria() {
        return addCreatureReady(player1, new MeriaScholarOfAntiquity());
    }

    private Permanent addArtifact() {
        return addCreatureReady(player1, new Ornithopter());
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
