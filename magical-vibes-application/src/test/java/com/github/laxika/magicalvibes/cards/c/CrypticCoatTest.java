package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrypticCoat.class, GrizzlyBears.class, Shock.class})
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

    private Permanent resolveCoat(Card topCard) {
        harness.setLibrary(player1, List.of(topCard));
        harness.castFromHand(player1, new CrypticCoat(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.isCloaked())
                .findFirst()
                .orElseThrow();
    }
}
