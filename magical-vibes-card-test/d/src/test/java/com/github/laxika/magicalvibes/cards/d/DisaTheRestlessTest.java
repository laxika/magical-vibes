package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AltarOfTheGoyf;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.Lhurgoyf;
import com.github.laxika.magicalvibes.cards.r.RatchetBomb;
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

@CardUsed({DisaTheRestless.class, AltarOfTheGoyf.class, Forest.class, GrizzlyBears.class,
        Lhurgoyf.class, RatchetBomb.class, Shock.class, TomeScour.class})
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
        Permanent token = findPermanent(player1, "Tarmogoyf");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);

        gd.playerGraveyards.get(player1.getId()).add(new Shock());
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
    }

    @Test
    void returnsMilledNoncreatureLhurgoyfPermanent() {
        addCreatureReady(player1, new DisaTheRestless());
        Card altar = new AltarOfTheGoyf();
        harness.setLibrary(player1, List.of(altar));
        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).contains(altar);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(altar);
    }

    @Test
    void returnsEveryLhurgoyfMilledTogether() {
        addCreatureReady(player1, new DisaTheRestless());
        Card first = new Lhurgoyf();
        Card second = new Lhurgoyf();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).contains(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second);
    }

    @Test
    void ignoresLhurgoyfMilledIntoOpponentsGraveyard() {
        addCreatureReady(player1, new DisaTheRestless());
        Card lhurgoyf = new Lhurgoyf();
        harness.setLibrary(player2, List.of(lhurgoyf));
        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(lhurgoyf);
        harness.assertNotOnBattlefield(player1, "Lhurgoyf");
        harness.assertNotOnBattlefield(player2, "Lhurgoyf");
    }

    @Test
    void doesNotReturnLhurgoyfThatDiesOnBattlefield() {
        addCreatureReady(player1, new DisaTheRestless());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        Permanent lhurgoyf = addCreatureReady(player1, new Lhurgoyf());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, lhurgoyf.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Lhurgoyf");
        harness.assertNotOnBattlefield(player1, "Lhurgoyf");
    }

    @Test
    void tokenCountsCardTypesInOpponentsGraveyardWithoutDisaAttacking() {
        addCreatureReady(player1, new DisaTheRestless());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player2, List.of(new Forest(), new Shock()));

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Tarmogoyf")).isEqualTo(1);
        Permanent token = findPermanent(player1, "Tarmogoyf");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
    }

    @Test
    void noncombatDamageDoesNotCreateToken() {
        addCreatureReady(player1, new DisaTheRestless());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Tarmogoyf")).isZero();
    }

    @Test
    void tarmogoyfTokenIsDestroyedByRatchetBombWithZeroChargeCounters() {
        addCreatureReady(player1, new DisaTheRestless());
        addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(1));
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Tarmogoyf")).isEqualTo(1);
        harness.addToBattlefield(player1, new RatchetBomb());
        int bombIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Ratchet Bomb"));

        harness.activateAbility(player1, bombIndex, 1, null, null);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Tarmogoyf")).isZero();
    }
}
