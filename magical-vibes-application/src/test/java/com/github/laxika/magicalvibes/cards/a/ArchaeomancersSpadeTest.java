package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FlaringPain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArchaeomancersSpade.class, FlaringPain.class, ThinkTwice.class, Shock.class})
class ArchaeomancersSpadeTest extends BaseCardTest {

    @Test
    void seeksTwoFlashbackCardsIntoTheGraveyard() {
        FlaringPain flaringPain = new FlaringPain();
        ThinkTwice thinkTwice = new ThinkTwice();
        Shock shock = new Shock();
        harness.setHand(player1, List.of(new ArchaeomancersSpade()));
        harness.setLibrary(player1, List.of(flaringPain, shock, thinkTwice));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(flaringPain, thinkTwice);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock);
    }

    @Test
    void addsRedAndWhiteManaRestrictedToSpellsCastFromOutsideTheHand() {
        harness.addToBattlefield(player1, new ArchaeomancersSpade());
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getNonHandSpellOnlyMana(ManaColor.RED))
                .isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getNonHandSpellOnlyMana(ManaColor.WHITE))
                .isEqualTo(1);
    }

    @Test
    void restrictedManaCannotPayForHandSpellButCanPayForFlashback() {
        harness.addToBattlefield(player1, new ArchaeomancersSpade());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of(new FlaringPain()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        FlaringPain flaringPain = new FlaringPain();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(flaringPain));
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getNonHandSpellOnlyMana(ManaColor.RED))
                .isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getNonHandSpellOnlyMana(ManaColor.WHITE))
                .isEqualTo(1);
    }
}
