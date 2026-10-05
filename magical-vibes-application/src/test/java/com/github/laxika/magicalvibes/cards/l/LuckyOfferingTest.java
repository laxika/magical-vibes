package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BruteSuit;
import com.github.laxika.magicalvibes.cards.c.ChromaticLantern;
import com.github.laxika.magicalvibes.cards.d.DreamstoneHedron;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TamiyosSafekeeping;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LuckyOffering.class, ChromaticLantern.class, DreamstoneHedron.class, GrizzlyBears.class,
        BruteSuit.class, TamiyosSafekeeping.class})
class LuckyOfferingTest extends BaseCardTest {

    @Test
    void canDestroyYourOwnArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BruteSuit());
        harness.setHand(player1, List.of(new LuckyOffering()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Brute Suit");
        harness.assertInGraveyard(player1, "Brute Suit");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    void gainsLifeEvenWhenLegalTargetIsIndestructible() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BruteSuit());
        harness.setHand(player1, List.of(new TamiyosSafekeeping(), new LuckyOffering()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        int lifeBefore = gd.getLife(player1.getId());

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Brute Suit");
        harness.assertNotInGraveyard(player1, "Brute Suit");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    void doesNotGainLifeWhenTargetGainsHexproofInResponse() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BruteSuit());
        harness.setHand(player1, List.of(new LuckyOffering()));
        harness.setHand(player2, List.of(new TamiyosSafekeeping()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castSorcery(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Brute Suit");
        harness.assertInGraveyard(player1, "Lucky Offering");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void destroysArtifactWithManaValueThreeAndGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChromaticLantern());
        harness.setHand(player1, List.of(new LuckyOffering()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Chromatic Lantern");
        harness.assertInGraveyard(player2, "Chromatic Lantern");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    void cannotTargetArtifactWithManaValueGreaterThanThree() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DreamstoneHedron());
        harness.setHand(player1, List.of(new LuckyOffering()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact with mana value 3 or less");
    }

    @Test
    void cannotTargetNonArtifactPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LuckyOffering()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact with mana value 3 or less");
    }
}
