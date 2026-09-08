package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CryptOfAgadeemTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new CryptOfAgadeem()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Crypt of Agadeem").isTapped()).isTrue();
    }

    @Test
    @DisplayName("First ability adds one black mana")
    void firstAbilityAddsBlackMana() {
        Permanent crypt = harness.addToBattlefieldAndReturn(player1, new CryptOfAgadeem());
        crypt.setSummoningSick(false);
        crypt.untap();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability adds black mana for each black creature card in the controller's graveyard")
    void secondAbilityAddsBlackManaForBlackCreatures() {
        harness.setGraveyard(player1, List.of(
                new BlackKnight(),
                new BlackKnight(),
                new GrizzlyBears(),
                new Mountain()
        ));
        Permanent crypt = harness.addToBattlefieldAndReturn(player1, new CryptOfAgadeem());
        crypt.setSummoningSick(false);
        crypt.untap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
    }

    @Test
    @DisplayName("Second ability ignores nonblack creatures, noncreatures, and the opponent's graveyard")
    void secondAbilityCountsOnlyOwnBlackCreatureCards() {
        harness.setGraveyard(player1, List.of(new BlackKnight(), new GrizzlyBears(), new Mountain()));
        harness.setGraveyard(player2, List.of(new BlackKnight(), new BlackKnight()));
        Permanent crypt = harness.addToBattlefieldAndReturn(player1, new CryptOfAgadeem());
        crypt.setSummoningSick(false);
        crypt.untap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }
}
