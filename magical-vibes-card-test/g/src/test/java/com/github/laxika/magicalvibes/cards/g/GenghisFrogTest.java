package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.f.FootNinjas;
import com.github.laxika.magicalvibes.cards.m.MichelangeloImproviser;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GenghisFrog.class, MichelangeloImproviser.class, FootNinjas.class, Conspiracy.class})
class GenghisFrogTest extends BaseCardTest {

    @Test
    void ownEntryCreatesMutagen() {
        harness.castFromHand(player1, new GenghisFrog(), "{G}{U}");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    void anotherMutantEntryCreatesMutagen() {
        harness.addToBattlefield(player1, new GenghisFrog());
        harness.castFromHand(player1, new MichelangeloImproviser(), "{3}{G}");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    void nonMutantEntryDoesNotCreateMutagen() {
        harness.addToBattlefield(player1, new GenghisFrog());
        harness.setHand(player1, List.of(new FootNinjas()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
    }

    @Test
    void opposingMutantEntryDoesNotCreateMutagen() {
        harness.addToBattlefield(player1, new GenghisFrog());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player2, new MichelangeloImproviser(), "{3}{G}");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
        assertThat(findPermanents(player2, "Mutagen")).isEmpty();
    }

    @Test
    void ownEntryCreatesMutagenEvenWhenItsCreatureTypeIsReplaced() {
        Permanent conspiracy = harness.addToBattlefieldAndReturn(player1, new Conspiracy());
        conspiracy.setChosenSubtype(CardSubtype.GOBLIN);

        harness.castFromHand(player1, new GenghisFrog(), "{G}{U}");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    void createdTokenHasMutagenArtifactSubtype() {
        createMutagen();

        assertThat(gqs.hasEffectiveSubtype(gd, findPermanent(player1, "Mutagen"), CardSubtype.MUTAGEN))
                .isTrue();
    }

    @Test
    void mutagenCanImmediatelyPutCounterOnOwnCreatureAndIsSacrificedAsCost() {
        createMutagen();
        Permanent frog = findPermanent(player1, "Genghis Frog");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, frog.getId());

        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
        assertThat(frog.getPlusOnePlusOneCounters()).isZero();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(frog.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    void mutagenCanTargetOpposingCreature() {
        createMutagen();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MichelangeloImproviser());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, target.getId());
        resolveAllTriggers();

        assertThat(target.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
    }

    @Test
    void mutagenCannotBeActivatedDuringCombat() {
        createMutagen();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null,
                findPermanent(player1, "Genghis Frog").getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    void mutagenCannotBeActivatedWithoutMana() {
        createMutagen();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null,
                findPermanent(player1, "Genghis Frog").getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    void mutagenCannotBeActivatedWhileASpellIsOnTheStack() {
        createMutagen();
        harness.castFromHand(player1, new MichelangeloImproviser(), "{3}{G}");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null,
                findPermanent(player1, "Genghis Frog").getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    void mutagenCannotBeActivatedDuringOpponentsMainPhase() {
        createMutagen();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null,
                findPermanent(player1, "Genghis Frog").getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    void tappedMutagenCannotBeActivated() {
        createMutagen();
        findPermanent(player1, "Mutagen").tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null,
                findPermanent(player1, "Genghis Frog").getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    void mutagenCannotTargetANoncreatureArtifact() {
        createMutagen();
        Permanent mutagen = findPermanent(player1, "Mutagen");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, mutagen.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    private void createMutagen() {
        harness.castFromHand(player1, new GenghisFrog(), "{G}{U}");
        resolveAllTriggers();
    }
}
