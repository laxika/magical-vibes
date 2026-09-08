package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EtaliPrimalConqueror.class, EtaliPrimalSickness.class, Forest.class, GrizzlyBears.class})
class EtaliPrimalConquerorTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by exiling each library until a nonland and offers all found spells")
    void entersByExilingUntilNonlandAndOffersFoundSpells() {
        Forest player1Land = new Forest();
        GrizzlyBears player1Spell = new GrizzlyBears();
        Forest player2Land = new Forest();
        GrizzlyBears player2Spell = new GrizzlyBears();
        Forest player1Remainder = new Forest();
        Forest player2Remainder = new Forest();
        harness.setLibrary(player1, List.of(player1Land, player1Spell, player1Remainder));
        harness.setLibrary(player2, List.of(player2Land, player2Spell, player2Remainder));
        harness.setHand(player1, List.of(new EtaliPrimalConqueror()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.ImprovisationCapstoneCastChoice interaction =
                (PendingInteraction.ImprovisationCapstoneCastChoice) gd.interaction.activeInteraction();
        assertThat(interaction.validCardIds())
                .containsExactlyInAnyOrder(player1Spell.getId(), player2Spell.getId());
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card())
                .containsExactlyInAnyOrder(player1Land, player1Spell, player2Land, player2Spell);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(player1Remainder);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(player2Remainder);
    }

    @Test
    @DisplayName("Transforms into Etali, Primal Sickness")
    void transformsIntoPrimalSickness() {
        Permanent etali = harness.addToBattlefieldAndReturn(player1, new EtaliPrimalConqueror());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(etali.getCard().getName()).isEqualTo("Etali, Primal Sickness");
        assertThat(etali.isTransformed()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Etali, Primal Sickness gives poison equal to combat damage dealt")
    void primalSicknessGivesPoisonEqualToCombatDamage() {
        Permanent etali = harness.addToBattlefieldAndReturn(player1, new EtaliPrimalConqueror());
        etali.setCard(etali.getOriginalCard().getBackFaceCard());
        etali.setTransformed(true);
        etali.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(9);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(11);
    }
}
