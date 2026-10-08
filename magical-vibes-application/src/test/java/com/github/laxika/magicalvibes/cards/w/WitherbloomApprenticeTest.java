package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.d.Duress;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PlatinumEmperion;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WitherbloomApprentice.class, Shock.class, BarkshellBlessing.class, GrizzlyBears.class,
        Duress.class, PlatinumEmperion.class})
class WitherbloomApprenticeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant triggers the life drain")
    void castingInstantTriggersLifeDrain() {
        addCreatureReady(player1, new WitherbloomApprentice());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Copying an instant triggers the life drain")
    void copyingInstantTriggersLifeDrain() {
        addCreatureReady(player1, new WitherbloomApprentice());
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, target.getId(), List.of(conspireA.getId(), conspireB.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Casting a sorcery triggers before the spell resolves")
    void castingSorceryTriggersLifeDrain() {
        addCreatureReady(player1, new WitherbloomApprentice());
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new Duress()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Opponent spells do not trigger magecraft")
    void opponentSpellDoesNotTrigger() {
        addCreatureReady(player1, new WitherbloomApprentice());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Creature spells do not trigger magecraft")
    void creatureSpellDoesNotTrigger() {
        addCreatureReady(player1, new WitherbloomApprentice());

        harness.castFromHand(player1, new WitherbloomApprentice(), "{B}{G}");
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Life gain is independent of whether the opponent can lose life")
    void gainsLifeWhenOpponentLifeTotalCannotChange() {
        addCreatureReady(player1, new WitherbloomApprentice());
        addCreatureReady(player2, new PlatinumEmperion());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }
}
