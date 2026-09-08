package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.t.TomeScour;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LazavTheMultifariousTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield surveils 1")
    void entersWithSurveil() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new LazavTheMultifarious()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Becomes a copy of a target creature card with mana value X, keeping its name, legendary supertype, and ability")
    void becomesCopyOfTargetCreatureCard() {
        Permanent lazav = addReadyLazav();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(lazav.getCard().getName()).isEqualTo("Lazav, the Multifarious");
        assertThat(lazav.getCard().getPower()).isEqualTo(2);
        assertThat(lazav.getCard().getToughness()).isEqualTo(2);
        assertThat(lazav.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);

        Card ornithopter = new Ornithopter();
        harness.setGraveyard(player1, List.of(ornithopter));
        harness.activateAbility(player1, 0, 0, ornithopter.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(lazav.getCard().getPower()).isZero();
        assertThat(lazav.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Rejects a noncreature or wrong-mana-value graveyard target")
    void rejectsIllegalGraveyardTarget() {
        addReadyLazav();
        Card sorcery = new TomeScour();
        harness.setGraveyard(player1, List.of(sorcery));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 2, sorcery.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        Card giant = new HillGiant();
        harness.setGraveyard(player1, List.of(giant));
        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 2, giant.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyLazav() {
        Permanent lazav = new Permanent(new LazavTheMultifarious());
        lazav.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(lazav);
        return lazav;
    }
}
