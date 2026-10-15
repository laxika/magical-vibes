package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChissGoriaForgeTyrant.class, Millstone.class, Ornithopter.class, GrizzlyBears.class})
class ChissGoriaForgeTyrantTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for artifacts reduces its own cost")
    void affinityForArtifactsReducesOwnCost() {
        harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.setHand(player1, List.of(new ChissGoriaForgeTyrant()));
        addMana(4, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Chiss-Goria, Forge Tyrant")).isNotNull();
    }

    @Test
    @DisplayName("Attacking exiles five cards and offers one artifact spell with affinity")
    void attackingOffersArtifactSpellWithAffinity() {
        Millstone millstone = new Millstone();
        GrizzlyBears creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(millstone, creature));
        harness.setHand(player1, List.of(new ChissGoriaForgeTyrant()));
        addMana(6, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent chiss = findPermanent(player1, "Chiss-Goria, Forge Tyrant");
        harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        chiss.setSummoningSick(false);
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(chiss.getId()))
                .extracting(Card::getId)
                .containsExactly(millstone.getId(), creature.getId());
        assertThat(gd.pendingMayAbilities)
                .extracting(PendingMayAbility::targetCardId)
                .containsExactly(millstone.getId());

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == millstone);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();

        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Millstone")).isNotNull();
    }

    private void addMana(int colorless, int red) {
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
        harness.addMana(player1, ManaColor.RED, red);
    }
}
