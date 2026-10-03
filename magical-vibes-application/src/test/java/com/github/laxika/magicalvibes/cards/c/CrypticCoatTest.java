package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrypticCoat.class, GrizzlyBears.class, Shock.class, TurnToFrog.class})
class CrypticCoatTest extends BaseCardTest {

    @Test
    void cloaksTopCardAndAttachesToIt() {
        Permanent cloaked = resolveCoat(new GrizzlyBears());
        Permanent coat = findPermanent(player1, "Cryptic Coat");

        assertThat(cloaked.isFaceDown()).isTrue();
        assertThat(cloaked.isCloaked()).isTrue();
        assertThat(coat.getAttachedTo()).isEqualTo(cloaked.getId());
        assertThat(gqs.getEffectivePower(gd, cloaked)).isEqualTo(3);
        assertThat(gqs.hasCantBeBlocked(gd, cloaked)).isTrue();
    }

    @Test
    void cloakedCreatureCanTurnFaceUpForItsManaCost() {
        Permanent cloaked = resolveCoat(new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cloaked));

        assertThat(cloaked.isFaceDown()).isFalse();
        assertThat(cloaked.isCloaked()).isFalse();
        assertThat(gqs.getEffectivePower(gd, cloaked)).isEqualTo(3);
    }

    @Test
    void cloakedCreatureHasWardTwo() {
        Permanent cloaked = resolveCoat(new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, cloaked.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(cloaked.isFaceDown()).isTrue();
    }

    @Test
    void canReturnEquipmentToItsOwnersHand() {
        resolveCoat(new GrizzlyBears());
        int coatIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Cryptic Coat"));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, coatIndex, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Cryptic Coat");
    }

    @Test
    void canCloakAnInstantButCannotTurnItFaceUpForItsManaCost() {
        Permanent cloaked = resolveCoat(new Shock());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.turnFaceUp(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(cloaked)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not a creature card");

        assertThat(cloaked.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, cloaked)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cloaked)).isEqualTo(2);
        assertThat(findPermanent(player1, "Cryptic Coat").getAttachedTo()).isEqualTo(cloaked.getId());
    }

    @Test
    void emptyLibraryLeavesEquipmentUnattached() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new CrypticCoat(), "{2}{U}");
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Cryptic Coat").getAttachedTo()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void returningCoatLeavesCloakedCreatureWithoutEquipmentBonuses() {
        Permanent cloaked = resolveCoat(new Shock());
        int coatIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Cryptic Coat"));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, coatIndex, 0, null, null);
        resolveAllTriggers();

        harness.assertInHand(player1, "Cryptic Coat");
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(cloaked);
        assertThat(cloaked.isCloaked()).isTrue();
        assertThat(gqs.getEffectivePower(gd, cloaked)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cloaked)).isEqualTo(2);
        assertThat(gqs.hasCantBeBlocked(gd, cloaked)).isFalse();
    }

    @Test
    void stillCloaksWhenEquipmentReturnsToHandBeforeItsTriggerResolves() {
        harness.setLibrary(player1, List.of(new Shock()));
        harness.castFromHand(player1, new CrypticCoat(), "{2}{U}");
        harness.passBothPriorities();
        int coatIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Cryptic Coat"));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, coatIndex, 0, null, null);
        resolveAllTriggers();

        harness.assertInHand(player1, "Cryptic Coat");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent cloaked = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(cloaked.isCloaked()).isTrue();
        assertThat(gqs.getEffectivePower(gd, cloaked)).isEqualTo(2);
        assertThat(gqs.hasCantBeBlocked(gd, cloaked)).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void cloakedCreatureCanTurnFaceUpAfterLosingAllAbilities() {
        Permanent cloaked = resolveCoat(new GrizzlyBears());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, cloaked.getId());
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cloaked));

        assertThat(cloaked.isFaceDown()).isFalse();
        assertThat(cloaked.isCloaked()).isFalse();
        assertThat(findPermanent(player1, "Cryptic Coat").getAttachedTo()).isEqualTo(cloaked.getId());
        assertThat(gqs.getEffectivePower(gd, cloaked)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cloaked)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, cloaked)).isTrue();
    }

    private Permanent resolveCoat(Card topCard) {
        harness.setLibrary(player1, List.of(topCard));
        harness.castFromHand(player1, new CrypticCoat(), "{2}{U}");
        resolveAllTriggers();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.isCloaked())
                .findFirst()
                .orElseThrow();
    }
}
