package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AnointedProcession;
import com.github.laxika.magicalvibes.cards.b.BladeSplicer;
import com.github.laxika.magicalvibes.cards.d.DoubleTrouble;
import com.github.laxika.magicalvibes.cards.f.FurnaceOfRath;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.t.ThoughtReflection;
import com.github.laxika.magicalvibes.cards.u.UnleashFury;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarosGoneNuts.class, AnointedProcession.class, BladeSplicer.class,
        FurnaceOfRath.class, LightningBolt.class, DoubleTrouble.class, ThoughtReflection.class, UnleashFury.class})
class MarosGoneNutsTest extends BaseCardTest {

    @Test
    @DisplayName("Maro's Gone Nuts quadruples an effect that doubles token creation")
    void quadruplesAnEffectThatDoublesTokens() {
        harness.addToBattlefield(player1, new MarosGoneNuts());
        harness.addToBattlefield(player1, new AnointedProcession());
        harness.setHand(player1, List.of(new BladeSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Phyrexian Golem")).hasSize(4);
    }

    @Test
    @DisplayName("Maro's Gone Nuts does not alter an effect that does not double")
    void doesNotAlterNonDoublingEffects() {
        harness.addToBattlefield(player1, new MarosGoneNuts());
        harness.setHand(player1, List.of(new BladeSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Phyrexian Golem")).hasSize(1);
    }

    @Test
    @DisplayName("Maro's Gone Nuts makes Furnace of Rath quadruple damage")
    void quadruplesGlobalDamageDoubler() {
        harness.addToBattlefield(player1, new MarosGoneNuts());
        harness.addToBattlefield(player1, new FurnaceOfRath());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(8);
    }

    @Test
    @DisplayName("Maro's Gone Nuts quadruples power rather than tripling it")
    void quadruplesPower() {
        harness.addToBattlefield(player1, new MarosGoneNuts());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BladeSplicer());

        harness.castFromHand(player1, new DoubleTrouble(), "{4}{R}");
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(creature.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Two Maro's Gone Nuts octuple power")
    void twoCopiesOctuplePower() {
        harness.addToBattlefield(player1, new MarosGoneNuts());
        harness.addToBattlefield(player2, new MarosGoneNuts());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BladeSplicer());

        harness.castFromHand(player1, new DoubleTrouble(), "{4}{R}");
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(8);
        assertThat(creature.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Maro's Gone Nuts modifies the opponent's token doubler too")
    void modifiesOpponentsTokenDoubler() {
        harness.addToBattlefield(player2, new MarosGoneNuts());
        harness.addToBattlefield(player1, new AnointedProcession());
        harness.setHand(player1, List.of(new BladeSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Phyrexian Golem")).hasSize(4);
        assertThat(findPermanents(player2, "Phyrexian Golem")).isEmpty();
    }

    @Test
    @DisplayName("Two Maro's Gone Nuts octuple token creation")
    void twoCopiesOctupleTokens() {
        harness.addToBattlefield(player1, new MarosGoneNuts());
        harness.addToBattlefield(player2, new MarosGoneNuts());
        harness.addToBattlefield(player1, new AnointedProcession());
        harness.setHand(player1, List.of(new BladeSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Phyrexian Golem")).hasSize(8);
    }

    @Test
    @DisplayName("Each draw doubler is independently quadrupled")
    void quadruplesEachDrawDoubler() {
        harness.addToBattlefield(player1, new MarosGoneNuts());
        harness.addToBattlefield(player1, new ThoughtReflection());
        harness.addToBattlefield(player1, new ThoughtReflection());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, java.util.stream.IntStream.range(0, 20)
                .mapToObj(i -> new LightningBolt()).toList());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(16);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Power doubling represented as a dynamic boost is quadrupled")
    void quadruplesUnleashFury() {
        harness.addToBattlefield(player1, new MarosGoneNuts());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BladeSplicer());
        harness.setHand(player1, List.of(new UnleashFury()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(creature.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("A face-down Maro's Gone Nuts has no doubling ability")
    void faceDownCopyDoesNotModifyDoubling() {
        Permanent faceDown = harness.addToBattlefieldAndReturn(player1, new MarosGoneNuts());
        faceDown.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addToBattlefield(player1, new FurnaceOfRath());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
    }
}
