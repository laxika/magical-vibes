package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DevilsPlay;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IvyElemental;
import com.github.laxika.magicalvibes.cards.k.KnollspineInvocation;
import com.github.laxika.magicalvibes.cards.s.SharkTyphoon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnboundFlourishing.class, DevilsPlay.class, GrizzlyBears.class, IvyElemental.class, KnollspineInvocation.class, SharkTyphoon.class})
class UnboundFlourishingTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles a permanent spell's X only when the triggered ability resolves")
    void doublesPermanentSpellX() {
        harness.addToBattlefield(player1, new UnboundFlourishing());
        harness.setHand(player1, List.of(new IvyElemental()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        gs.playCard(gd, player1, 0, 2, null, null);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(2);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(4);
        harness.passBothPriorities();

        Permanent ivy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Ivy Elemental"))
                .findFirst()
                .orElseThrow();
        assertThat(ivy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Copies an X instant or sorcery spell")
    void copiesXSpell() {
        harness.addToBattlefield(player1, new UnboundFlourishing());
        harness.setHand(player1, List.of(new DevilsPlay()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, false);
        }
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Copies an activated ability")
    void copiesActivatedAbility() {
        harness.addToBattlefield(player1, new UnboundFlourishing());
        harness.addToBattlefield(player1, new KnollspineInvocation());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, 2, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, false);
        }
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Copies spells even when X is zero")
    void copiesZeroXSpell() {
        harness.addToBattlefield(player1, new UnboundFlourishing());
        harness.setHand(player1, List.of(new DevilsPlay()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0, player2.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Devil's Play");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The spell copy may have a new target while the original keeps its target")
    void retargetsSpellCopy() {
        harness.addToBattlefield(player1, new UnboundFlourishing());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DevilsPlay()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The ability copy may have a new target without paying or discarding again")
    void retargetsAbilityCopy() {
        harness.addToBattlefield(player1, new UnboundFlourishing());
        harness.addToBattlefield(player1, new KnollspineInvocation());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, 2, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not copy a spell whose mana cost has no X")
    void ignoresNonXSpell() {
        harness.addToBattlefield(player1, new UnboundFlourishing());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof GrizzlyBears)
                .hasSize(1);
    }

    @Test
    @DisplayName("Does not double X in an opponent's permanent spell")
    void ignoresOpponentPermanentSpell() {
        harness.addToBattlefield(player2, new UnboundFlourishing());
        harness.setHand(player1, List.of(new IvyElemental()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        gs.playCard(gd, player1, 0, 2, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(2);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement()
                .satisfies(permanent -> assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(2));
    }

    @Test
    @DisplayName("Copies an X cycling ability activated from hand")
    void copiesXHandAbility() {
        harness.addToBattlefield(player1, new UnboundFlourishing());
        harness.setHand(player1, List.of(new SharkTyphoon()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateHandAbility(player1, 0, null, 2);

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        for (int round = 0; round < 8 && !gd.stack.isEmpty(); round++) {
            harness.passBothPriorities();
        }

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Shark Typhoon");
    }
}
