package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.Lhurgoyf;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TomeScour;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DisaTheRestless.class, Forest.class, GrizzlyBears.class, Lhurgoyf.class, Shock.class,
        TomeScour.class})
class DisaTheRestlessTest extends BaseCardTest {

    @Test
    @DisplayName("puts a Lhurgoyf permanent card milled from a library onto the battlefield")
    void putsMilledLhurgoyfOntoBattlefield() {
        addCreatureReady(player1, new DisaTheRestless());
        Card lhurgoyf = new Lhurgoyf();
        harness.setLibrary(player1, List.of(lhurgoyf));
        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == lhurgoyf);
    }

    @Test
    @DisplayName("does not return a creature that is not a Lhurgoyf")
    void ignoresOtherCreatureCards() {
        addCreatureReady(player1, new DisaTheRestless());
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears);
    }

    @Test
    @DisplayName("creates one dynamic Tarmogoyf token for combat damage by multiple creatures")
    void createsOneDynamicTarmogoyfToken() {
        addCreatureReady(player1, new DisaTheRestless());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Tarmogoyf")).isEqualTo(1);
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> "Tarmogoyf".equals(permanent.getCard().getName()))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);

        gd.playerGraveyards.get(player1.getId()).add(new Shock());
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
    }
}
