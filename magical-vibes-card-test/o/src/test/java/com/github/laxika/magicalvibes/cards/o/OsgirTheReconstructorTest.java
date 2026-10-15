package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BronzeGuardian;
import com.github.laxika.magicalvibes.cards.c.Cloudshift;
import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({OsgirTheReconstructor.class, DarksteelRelic.class, GrizzlyBears.class,
        SolRing.class, Spellbook.class, BronzeGuardian.class, Cloudshift.class})
class OsgirTheReconstructorTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an artifact gives a creature +2/+0 until end of turn")
    void sacrificesArtifactAndBoostsTargetCreature() {
        Permanent osgir = harness.addToBattlefieldAndReturn(player1, new OsgirTheReconstructor());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact.getCard());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(osgir.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The pump ability cannot target an opponent's creature")
    void pumpAbilityRequiresCreatureYouControl() {
        Permanent osgir = harness.addToBattlefieldAndReturn(player1, new OsgirTheReconstructor());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new DarksteelRelic());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
        assertThat(osgir.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Exiling an artifact with mana value X creates two token copies")
    void createsTwoCopiesOfArtifactWithMatchingManaValue() {
        Permanent osgir = harness.addToBattlefieldAndReturn(player1, new OsgirTheReconstructor());
        osgir.setSummoningSick(false);
        Card spellbook = new Spellbook();
        Card solRing = new SolRing();
        harness.setGraveyard(player1, List.of(spellbook, solRing));

        harness.activateAbility(player1, 0, 1, 0, null);

        PendingInteraction.GraveyardExileCostChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardExileCostChoice.class);
        assertThat(choice.validIndices()).containsExactly(0);

        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spellbook);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(solRing);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Spellbook")))
                .hasSize(2);
    }

    @Test
    @DisplayName("The copy ability cannot use an artifact with a different mana value")
    void copyAbilityRequiresMatchingManaValue() {
        Permanent osgir = harness.addToBattlefieldAndReturn(player1, new OsgirTheReconstructor());
        osgir.setSummoningSick(false);
        harness.setGraveyard(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 1, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }

    @Test
    @DisplayName("The copy ability is sorcery speed only")
    void copyAbilityIsSorcerySpeedOnly() {
        harness.addToBattlefield(player1, new OsgirTheReconstructor());
        harness.setGraveyard(player1, List.of(new Spellbook()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void nonzeroXIsPaidAndCopiesRetainActivatedAbilities() {
        Permanent osgir = harness.addToBattlefieldAndReturn(player1, new OsgirTheReconstructor());
        osgir.setSummoningSick(false);
        Card solRing = new SolRing();
        harness.setGraveyard(player1, List.of(solRing));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, 1, null);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(osgir.isTapped()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(solRing);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        List<Permanent> copies = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
        assertThat(copies).hasSize(2);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.activateAbility(player1, 2, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
    }

    @Test
    void artifactCreatureCopiesRetainTheirContinuousAbilities() {
        Permanent osgir = harness.addToBattlefieldAndReturn(player1, new OsgirTheReconstructor());
        osgir.setSummoningSick(false);
        harness.setGraveyard(player1, List.of(new BronzeGuardian()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, 5, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        List<Permanent> copies = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
        assertThat(copies).hasSize(2);
        assertThat(copies).allSatisfy(copy -> assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(2));
    }

    @Test
    void copyAbilityCannotBeActivatedWhileSummoningSick() {
        harness.addToBattlefield(player1, new OsgirTheReconstructor());
        Card spellbook = new Spellbook();
        harness.setGraveyard(player1, List.of(spellbook));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spellbook);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copyAbilityCannotUseOpponentsGraveyardOrNonartifactCards() {
        Permanent osgir = harness.addToBattlefieldAndReturn(player1, new OsgirTheReconstructor());
        osgir.setSummoningSick(false);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new SolRing()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 1, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 2, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(osgir.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void pumpAbilityWorksOnOpponentsTurnWhileOsgirIsTappedAndSummoningSick() {
        Permanent osgir = harness.addToBattlefieldAndReturn(player1, new OsgirTheReconstructor());
        osgir.tap();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SolRing());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        harness.activateAbility(player1, 0, 0, null, osgir.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact.getCard());
        assertThat(gqs.getEffectivePower(gd, osgir)).isEqualTo(4);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, osgir)).isEqualTo(6);
        assertThat(osgir.isTapped()).isTrue();
    }

    @Test
    void copyAbilityCannotBeActivatedWhileTapped() {
        Permanent osgir = harness.addToBattlefieldAndReturn(player1, new OsgirTheReconstructor());
        osgir.setSummoningSick(false);
        osgir.tap();
        Card solRing = new SolRing();
        harness.setGraveyard(player1, List.of(solRing));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 1, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(solRing);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void blinkingOsgirDoesNotPreventCopiesOfTheAlreadyExiledCard() {
        Permanent osgir = harness.addToBattlefieldAndReturn(player1, new OsgirTheReconstructor());
        osgir.setSummoningSick(false);
        Card solRing = new SolRing();
        harness.setGraveyard(player1, List.of(solRing));
        harness.setHand(player1, List.of(new Cloudshift()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, 1, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.castAndResolveInstant(player1, 0, osgir.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(solRing);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Sol Ring")))
                .hasSize(2);
    }
}
