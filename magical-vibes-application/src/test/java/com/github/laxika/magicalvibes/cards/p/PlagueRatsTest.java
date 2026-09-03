package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.ImprisonedInTheMoon;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlagueRats.class, GrizzlyBears.class, ImprisonedInTheMoon.class})
class PlagueRatsTest extends BaseCardTest {

    @Test
    @DisplayName("Plague Rats is 1/1 when it is the only one on the battlefield")
    void isOneOneWhenAlone() {
        Permanent rats = addPlagueRats(player1);

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rats)).isEqualTo(1);
    }

    @Test
    void ignoresCreaturesWithOtherNames() {
        Permanent rats = addPlagueRats(player1);
        addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rats)).isEqualTo(1);
    }

    @Test
    @DisplayName("Plague Rats P/T equal the number of Plague Rats on the battlefield")
    void ptEqualsTotalPlagueRats() {
        Permanent rats = addPlagueRats(player1);
        addPlagueRats(player1);
        addPlagueRats(player1);

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, rats)).isEqualTo(3);
    }

    @Test
    @DisplayName("Plague Rats counts copies controlled by any player")
    void countsAllPlayersPlagueRats() {
        Permanent rats = addPlagueRats(player1);
        addPlagueRats(player2);

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rats)).isEqualTo(2);
    }

    @Test
    @DisplayName("Plague Rats P/T updates as other Plague Rats leave the battlefield")
    void ptUpdatesWhenRatsLeave() {
        Permanent rats = addPlagueRats(player1);
        addPlagueRats(player1);

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p != rats && p.getCard().getName().equals("Plague Rats"));

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rats)).isEqualTo(1);
    }

    @Test
    void doesNotCountNamedNoncreaturePermanent() {
        Permanent rats = addPlagueRats(player1);
        Permanent enchantedRats = addPlagueRats(player1);

        Permanent imprisonment = harness.addToBattlefieldAndReturn(player2, new ImprisonedInTheMoon());
        imprisonment.setAttachedTo(enchantedRats.getId());

        assertThat(gqs.isCreature(gd, enchantedRats)).isFalse();
        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rats)).isEqualTo(1);
    }

    private Permanent addPlagueRats(Player player) {
        return addCreatureReady(player, new PlagueRats());
    }
}
