package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PrismaticOmen;
import com.github.laxika.magicalvibes.cards.r.RealityAcid;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EstridTheMasked.class, Forest.class, GrizzlyBears.class, PrismaticOmen.class, RealityAcid.class})
class EstridTheMaskedTest extends BaseCardTest {

    @Test
    @DisplayName("+2 untaps each enchanted permanent controlled by Estrid's controller")
    void plusTwoUntapsControlledEnchantedPermanents() {
        Permanent estrid = addReadyEstrid(3);
        Permanent enchantedLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RealityAcid());
        aura.setAttachedTo(enchantedLand.getId());
        enchantedLand.tap();
        Permanent unenchantedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        unenchantedCreature.tap();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(enchantedLand.isTapped()).isFalse();
        assertThat(unenchantedCreature.isTapped()).isTrue();
        assertThat(estrid.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("-1 creates and attaches a Mask Aura to another noncreature permanent")
    void minusOneCreatesMaskAttachedToPermanent() {
        addReadyEstrid(3);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, 0, 1, null, land.getId());
        harness.passBothPriorities();

        Permanent mask = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(mask.getCard().getName()).isEqualTo("Mask");
        assertThat(mask.getCard().isAura()).isTrue();
        assertThat(mask.getAttachedTo()).isEqualTo(land.getId());
    }

    @Test
    @DisplayName("-1 cannot target Estrid itself")
    void minusOneRequiresAnotherPermanent() {
        Permanent estrid = addReadyEstrid(3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, estrid.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-7 mills seven, returns non-Auras first, then attaches returned Auras")
    void minusSevenMillsAndReturnsEnchantmentsInOrder() {
        addReadyEstrid(7);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Card nonAuraEnchantment = new PrismaticOmen();
        Card aura = new RealityAcid();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(nonAuraEnchantment, aura, creature));
        List<Card> library = List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest());
        harness.setLibrary(player1, library);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, land.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == nonAuraEnchantment);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == aura
                        && permanent.getAttachedTo().equals(land.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(creature)
                .containsAll(library);
    }

    private Permanent addReadyEstrid(int loyalty) {
        Permanent perm = new Permanent(new EstridTheMasked());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
