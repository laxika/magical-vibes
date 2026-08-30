package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AquaticIncursionTest extends BaseCardTest {

    @Test
    @DisplayName("When Aquatic Incursion enters, it creates two blue hexproof Merfolk tokens")
    void createsHexproofMerfolkTokens() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new AquaticIncursion()));
        addAquaticIncursionMana(player1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.MERFOLK);
            assertThat(gqs.hasKeyword(gd, token, Keyword.HEXPROOF)).isTrue();
        });
    }

    @Test
    @DisplayName("The ability makes a target Merfolk unblockable this turn")
    void makesTargetMerfolkUnblockable() {
        Permanent incursion = addReadyIncursion(player1);
        Permanent merfolk = addReadyCreature(player1, new CoralMerfolk());
        addAquaticIncursionMana(player1);

        harness.activateAbility(player1, battlefieldIndex(player1, incursion), null, merfolk.getId());
        harness.passBothPriorities();

        assertThat(merfolk.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(merfolk.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("The ability cannot target a non-Merfolk creature")
    void cannotTargetNonMerfolkCreature() {
        Permanent incursion = addReadyIncursion(player1);
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        addAquaticIncursionMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, incursion), null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyIncursion(Player player) {
        Permanent incursion = harness.addToBattlefieldAndReturn(player, new AquaticIncursion());
        incursion.setSummoningSick(false);
        return incursion;
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, card);
        creature.setSummoningSick(false);
        return creature;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private void addAquaticIncursionMana(Player player) {
        harness.addMana(player, ManaColor.BLUE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 3);
    }
}
