package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JunkyardGeniusTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a tapped Powerstone token")
    void entersWithTappedPowerstone() {
        harness.setHand(player1, List.of(new JunkyardGenius()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent powerstone = findPermanent(player1, "Powerstone");
        assertThat(powerstone.isTapped()).isTrue();
        assertThat(powerstone.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(powerstone.getCard().getSubtypes()).contains(CardSubtype.POWERSTONE);
    }

    @Test
    @DisplayName("Sacrificing another creature boosts other creatures with menace and haste")
    void activationSacrificesCreatureAndBoostsOtherCreatures() {
        Permanent genius = addReady(new JunkyardGenius());
        Permanent sacrificed = addReady(new GrizzlyBears());
        Permanent ally = addReady(new GrizzlyBears());
        Permanent opponent = addReadyOpponent(new GrizzlyBears());
        addActivationMana();

        harness.activateAbility(player1, battlefieldIndex(genius), null, null);

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(sacrificed.getId(), ally.getId());
        assertThat(choice.validIds()).doesNotContain(genius.getId(), opponent.getId());

        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificed.getCard());
        assertThat(ally.getPowerModifier()).isEqualTo(1);
        assertThat(ally.hasKeyword(Keyword.MENACE)).isTrue();
        assertThat(ally.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(genius.getPowerModifier()).isZero();
        assertThat(genius.hasKeyword(Keyword.MENACE)).isFalse();
        assertThat(genius.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(opponent.getPowerModifier()).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ally.getPowerModifier()).isZero();
        assertThat(ally.hasKeyword(Keyword.MENACE)).isFalse();
        assertThat(ally.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The activation can sacrifice another artifact")
    void activationSacrificesArtifact() {
        Permanent genius = addReady(new JunkyardGenius());
        Permanent ally = addReady(new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        addActivationMana();

        harness.activateAbility(player1, battlefieldIndex(genius), null, null);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact.getCard());
        assertThat(ally.getPowerModifier()).isEqualTo(1);
        assertThat(ally.hasKeyword(Keyword.MENACE)).isTrue();
        assertThat(ally.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The activation cannot sacrifice Junkyard Genius itself")
    void activationRequiresAnotherPermanent() {
        Permanent genius = addReady(new JunkyardGenius());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(genius), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 2);
    }

    private Permanent addReady(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addReadyOpponent(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
