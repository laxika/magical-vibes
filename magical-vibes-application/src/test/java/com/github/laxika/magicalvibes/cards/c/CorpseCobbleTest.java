package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CorpseCobble.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class CorpseCobbleTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Zombie whose power and toughness equal sacrificed creatures' total power")
    void createsZombieWithTotalSacrificedPower() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new CorpseCobble()));
        addNormalMana();

        harness.castInstantWithSacrifices(player1, 0, null, List.of(bears.getId(), hillGiant.getId()));
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getEffectivePower()).isEqualTo(5);
        assertThat(token.getEffectiveToughness()).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, token, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Flashback also requires sacrificing creatures and creates the scaled Zombie")
    void flashbackUsesAdditionalCost() {
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setGraveyard(player1, List.of(new CorpseCobble()));
        addFlashbackMana();

        harness.castFlashbackWithSacrifices(player1, 0, null, List.of(hillGiant.getId()));
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getEffectivePower()).isEqualTo(3);
        assertThat(token.getEffectiveToughness()).isEqualTo(3);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Corpse Cobble"));
    }

    @Test
    @DisplayName("Rejects a noncreature for the additional sacrifice cost")
    void rejectsNoncreatureSacrifice() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new CorpseCobble()));
        addNormalMana();

        assertThatThrownBy(() -> harness.castInstantWithSacrifices(player1, 0, null, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addNormalMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    private void addFlashbackMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
