package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FierceGuardianship.class, GrizzlyBears.class, MightOfOaks.class, EdgarMarkov.class, Opt.class})
class FierceGuardianshipTest extends BaseCardTest {

    @Test
    void countersNoncreatureSpell() {
        Opt opt = new Opt();
        harness.setHand(player1, List.of(opt));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.setHand(player2, List.of(new FierceGuardianship()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, opt.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Opt");
        harness.assertInGraveyard(player2, "Fierce Guardianship");
    }

    @Test
    void commanderAllowsCastingWithoutPayingManaCost() {
        addToCommandZone(player1, new EdgarMarkov());
        harness.addToBattlefield(player1, new EdgarMarkov());

        Opt opt = new Opt();
        harness.setHand(player2, List.of(opt));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.setHand(player1, List.of(new FierceGuardianship()));
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0);
        harness.passPriority(player2);
        harness.castInstantWithAlternateCost(player1, 0, opt.getId(), List.of());
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player1, "Fierce Guardianship");
        harness.assertInGraveyard(player2, "Opt");
    }

    @Test
    void cannotUseFreeCastWithoutControllingRegisteredCommander() {
        addToCommandZone(player1, new EdgarMarkov());
        harness.setHand(player1, List.of(new FierceGuardianship()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player1, 0, null, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotTargetCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new FierceGuardianship()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("noncreature spell");
    }

    private void addToCommandZone(Player player, Card card) {
        gd.playerCommandZones.get(player.getId()).add(card);
    }
}
