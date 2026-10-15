package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
import com.github.laxika.magicalvibes.cards.e.EnergyRefractor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JunkyardGenius.class, ArgothianSprite.class, EnergyRefractor.class})
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
        Permanent genius = addCreatureReady(player1, new JunkyardGenius());
        Permanent sacrificed = addCreatureReady(player1, new ArgothianSprite());
        Permanent ally = addCreatureReady(player1, new ArgothianSprite());
        Permanent opponent = addCreatureReady(player2, new ArgothianSprite());
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
        Permanent genius = addCreatureReady(player1, new JunkyardGenius());
        Permanent ally = addCreatureReady(player1, new ArgothianSprite());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new EnergyRefractor());
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
        Permanent genius = addCreatureReady(player1, new JunkyardGenius());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(genius), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("The ability resolves after its source is sacrificed to another activation")
    void abilityResolvesWithoutItsSource() {
        Permanent genius = harness.addToBattlefieldAndReturn(player1, new JunkyardGenius());
        Permanent secondGenius = harness.addToBattlefieldAndReturn(player1, new JunkyardGenius());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new EnergyRefractor());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        addActivationMana();
        addActivationMana();

        harness.activateAbility(player1, battlefieldIndex(genius), null, null);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.activateAbility(player1, battlefieldIndex(secondGenius), null, null);
        harness.handlePermanentChosen(player1, genius.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ally.getPowerModifier()).isEqualTo(2);
        assertThat(ally.getToughnessModifier()).isZero();
        assertThat(ally.hasKeyword(Keyword.MENACE)).isTrue();
        assertThat(ally.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(secondGenius.getPowerModifier()).isEqualTo(1);
        assertThat(secondGenius.hasKeyword(Keyword.MENACE)).isTrue();
        assertThat(secondGenius.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Creatures are selected on resolution and later creatures receive no bonus")
    void affectsOnlyCreaturesPresentAtResolution() {
        Permanent genius = harness.addToBattlefieldAndReturn(player1, new JunkyardGenius());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new EnergyRefractor());
        addActivationMana();

        harness.activateAbility(player1, battlefieldIndex(genius), null, null);
        harness.handlePermanentChosen(player1, artifact.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact.getCard());
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());

        assertThat(beforeResolution.getPowerModifier()).isEqualTo(1);
        assertThat(beforeResolution.getToughnessModifier()).isZero();
        assertThat(beforeResolution.hasKeyword(Keyword.MENACE)).isTrue();
        assertThat(beforeResolution.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(afterResolution.getPowerModifier()).isZero();
        assertThat(afterResolution.hasKeyword(Keyword.MENACE)).isFalse();
        assertThat(afterResolution.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The tapped Powerstone created by the trigger can pay the sacrifice cost")
    void canSacrificeItsTappedPowerstone() {
        harness.setHand(player1, List.of(new JunkyardGenius()));
        addActivationMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent genius = findPermanent(player1, "Junkyard Genius");
        Permanent powerstone = findPermanent(player1, "Powerstone");
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        addActivationMana();

        harness.activateAbility(player1, battlefieldIndex(genius), null, null);
        harness.handlePermanentChosen(player1, powerstone.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Powerstone");
        assertThat(ally.getPowerModifier()).isEqualTo(1);
        assertThat(ally.hasKeyword(Keyword.MENACE)).isTrue();
        assertThat(ally.hasKeyword(Keyword.HASTE)).isTrue();
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 2);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
