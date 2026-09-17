package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
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
        harness.passBothPriorities();
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

    private Permanent addMeria() {
        Permanent meria = harness.addToBattlefieldAndReturn(player1, new MeriaScholarOfAntiquity());
        meria.setSummoningSick(false);
        return meria;
    }

    private Permanent addArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        artifact.setSummoningSick(false);
        return artifact;
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
